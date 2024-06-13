package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult
import nebulosa.api.cameras.*
import nebulosa.api.messages.MessageEvent
import nebulosa.api.mounts.MountMoveRequest
import nebulosa.api.mounts.MountMoveTask
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.PauseListener
import nebulosa.common.time.Stopwatch
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.platesolver.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.hypot

data class TPPATask(
    @JvmField val camera: Camera,
    @JvmField val solver: PlateSolver,
    @JvmField val request: TPPAStartRequest,
    @JvmField val mount: Mount? = null,
    @JvmField val longitude: Angle = mount!!.longitude,
    @JvmField val latitude: Angle = mount!!.latitude,
) : AbstractTask<MessageEvent>(), Consumer<Any>, PauseListener, CameraEventAware {

    @JvmField val mountMoveRequest = MountMoveRequest(request.stepDirection, request.stepDuration, request.stepSpeed)

    @JvmField val cameraRequest = request.capture.copy(
        savePath = CAPTURE_SAVE_PATH,
        exposureAmount = 0, exposureDelay = Duration.ZERO,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    private val cameraCaptureTask = CameraCaptureTask(camera, cameraRequest, exposureMaxRepeat = 1)
    private val settleDelayTask = DelayTask(SETTLE_TIME)
    private val mountMoveState = BooleanArray(3)
    private val elapsedTime = Stopwatch()
    private val pausing = AtomicBoolean()
    private val finished = AtomicBoolean()

    @Volatile private var rightAscension: Angle = 0.0
    @Volatile private var declination: Angle = 0.0
    @Volatile private var azimuthError: Angle = 0.0
    @Volatile private var altitudeError: Angle = 0.0
    @Volatile private var totalError: Angle = 0.0
    @Volatile private var azimuthErrorDirection = ""
    @Volatile private var altitudeErrorDirection = ""
    @Volatile private var savedImage: Path? = null
    @Volatile private var noSolutionAttempts = 0
    @Volatile private var captureEvent: CameraCaptureEvent? = null

    init {
        cameraCaptureTask.subscribe(this)
        settleDelayTask.subscribe(this)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        if (camera === event.device) {
            cameraCaptureTask.handleCameraEvent(event)
        }
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is TPPAEvent

    override fun accept(event: Any) {
        when (event) {
            is CameraCaptureEvent -> {
                captureEvent = event

                if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
                    savedImage = event.savedPath!!
                }

                if (!finished.get()) {
                    sendEvent(TPPAState.EXPOSURING, event)
                }
            }
            is DelayEvent -> {
                sendEvent(TPPAState.SETTLING)
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info(
            "TPPA started. longitude={}, latitude={}, rightAscension={}, declination={}, camera={}, mount={}, request={}",
            longitude.formatSignedDMS(), latitude.formatSignedDMS(), mount?.rightAscension?.formatHMS(), mount?.declination?.formatSignedDMS(),
            camera, mount, request
        )

        finished.set(false)
        elapsedTime.start()

        rightAscension = mount?.rightAscension ?: 0.0
        declination = mount?.declination ?: 0.0

        camera.snoop(camera.snoopedDevices.filter { it !is Mount } + mount)

        cancellationToken.listenToPause(this)

        while (!cancellationToken.isCancelled) {
            if (cancellationToken.isPaused) {
                pausing.set(false)
                sendEvent(TPPAState.PAUSED)
                cancellationToken.waitForPause()
            }

            if (cancellationToken.isCancelled) break

            mount?.tracking(true)

            // SLEWING.
            if (mount != null) {
                if (alignment.state.ordinal in 1..2 && !mountMoveState[alignment.state.ordinal]) {
                    MountMoveTask(mount, mountMoveRequest).use {
                        sendEvent(TPPAState.SLEWING)
                        it.execute(cancellationToken)
                        mountMoveState[alignment.state.ordinal] = true
                    }

                    if (cancellationToken.isCancelled) break

                    rightAscension = mount.rightAscension
                    declination = mount.declination
                    sendEvent(TPPAState.SLEWED)

                    LOG.info("TPPA slewed. rightAscension={}, declination={}", mount.rightAscension.formatHMS(), mount.declination.formatSignedDMS())

                    settleDelayTask.execute(cancellationToken)
                }
            }

            if (cancellationToken.isCancelled) break

            sendEvent(TPPAState.EXPOSURING)

            // CAPTURE.
            cameraCaptureTask.execute(cancellationToken)

            if (cancellationToken.isCancelled || savedImage == null) {
                break
            }

            sendEvent(TPPAState.SOLVING)

            // ALIGNMENT.
            val radius = if (mount == null) 0.0 else ATTEMPT_RADIUS * (noSolutionAttempts + 1)

            val result = try {
                alignment.align(
                    savedImage!!, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius,
                    request.compensateRefraction, cancellationToken
                )
            } catch (e: Throwable) {
                sendEvent(TPPAState.FAILED)
                LOG.error("failed to align", e)
                break
            }

            savedImage = null

            LOG.info("TPPA alignment completed. result=$result")

            if (cancellationToken.isCancelled) break

            when (result) {
                is ThreePointPolarAlignmentResult.NeedMoreMeasurement -> {
                    noSolutionAttempts = 0
                    rightAscension = result.rightAscension
                    declination = result.declination
                    sendEvent(TPPAState.SOLVED)
                    continue
                }
                is ThreePointPolarAlignmentResult.NoPlateSolution -> {
                    noSolutionAttempts++

                    sendEvent(TPPAState.FAILED)

                    if (noSolutionAttempts < MAX_ATTEMPTS) {
                        continue
                    } else {
                        LOG.error("exhausted all attempts to plate solve")
                        break
                    }
                }
                is ThreePointPolarAlignmentResult.Measured -> {
                    noSolutionAttempts = 0

                    rightAscension = result.rightAscension
                    declination = result.declination
                    azimuthError = result.azimuth
                    altitudeError = result.altitude
                    totalError = hypot(azimuthError, altitudeError)

                    azimuthErrorDirection = when {
                        azimuthError > 0 -> if (latitude > 0) "🠔 Move LEFT/WEST" else "🠔 Move LEFT/EAST"
                        azimuthError < 0 -> if (latitude > 0) "Move RIGHT/EAST 🠖" else "Move RIGHT/WEST 🠖"
                        else -> ""
                    }

                    altitudeErrorDirection = when {
                        altitudeError > 0 -> if (latitude > 0) "🠗 Move DOWN" else "Move UP 🠕"
                        altitudeError < 0 -> if (latitude > 0) "Move UP 🠕" else "🠗 Move DOWN"
                        else -> ""
                    }

                    LOG.info(
                        "TPPA alignment computed. rightAscension={}, declination={}, azimuthError={}, altitudeError={}",
                        result.rightAscension.formatHMS(), result.declination.formatSignedDMS(),
                        azimuthError.formatSignedDMS(), altitudeError.formatSignedDMS(),
                    )

                    sendEvent(TPPAState.COMPUTED)

                    continue
                }
                is ThreePointPolarAlignmentResult.Cancelled -> {
                    break
                }
            }
        }

        pausing.set(false)
        cancellationToken.unlistenToPause(this)

        finished.set(true)
        elapsedTime.stop()

        if (request.stopTrackingWhenDone) {
            mount?.tracking(false)
        }

        sendEvent(TPPAState.FINISHED)

        LOG.info("TPPA finished. camera={}, mount={}, request={}", camera, mount, request)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun processCameraCaptureEvent(event: CameraCaptureEvent?): CameraCaptureEvent? {
        return event?.copy(captureElapsedTime = elapsedTime.elapsed)
    }

    private fun sendEvent(state: TPPAState, capture: CameraCaptureEvent? = captureEvent) {
        val event = TPPAEvent(
            camera, if (pausing.get()) TPPAState.PAUSING else state, rightAscension, declination,
            azimuthError, altitudeError, totalError,
            azimuthErrorDirection, altitudeErrorDirection,
            processCameraCaptureEvent(capture),
        )

        onNext(event)

        if (capture?.state == CameraCaptureState.EXPOSURE_FINISHED) {
            onNext(capture)
        }
    }

    override fun reset() {
        mountMoveState.fill(false)
        azimuthError = 0.0
        altitudeError = 0.0
        totalError = 0.0
        azimuthErrorDirection = ""
        altitudeErrorDirection = ""
        savedImage = null
        noSolutionAttempts = 0

        pausing.set(false)
        finished.set(false)
        elapsedTime.reset()

        cameraCaptureTask.reset()
        settleDelayTask.reset()

        alignment.reset()

        super.reset()
    }

    override fun onPause(paused: Boolean) {
        pausing.set(paused)

        if (paused) {
            sendEvent(TPPAState.PAUSING)
        }
    }

    override fun close() {
        cameraCaptureTask.close()
        super.close()
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val SETTLE_TIME = Duration.ofSeconds(5)
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("tppa-")
        @JvmStatic private val LOG = loggerFor<TPPATask>()

        const val MAX_ATTEMPTS = 30
        const val ATTEMPT_RADIUS = ThreePointPolarAlignment.DEFAULT_RADIUS / 2.0
    }
}
