package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureState
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.messages.MessageEvent
import nebulosa.api.mounts.MountMoveTask
import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.time.Stopwatch
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.plate.solving.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

data class TPPATask(
    @JvmField val camera: Camera,
    @JvmField val solver: PlateSolver,
    @JvmField val request: TPPAStartRequest,
    @JvmField val mount: Mount? = null,
    @JvmField val longitude: Angle = mount!!.longitude,
    @JvmField val latitude: Angle = mount!!.latitude,
) : Task<MessageEvent>(), Consumer<CameraCaptureEvent> {

    @JvmField val guideRequest = GuidePulseRequest(request.stepDirection, request.stepDuration)
    @JvmField val cameraRequest = request.capture.copy(
        savePath = Files.createTempDirectory("tppa"),
        exposureAmount = 0, exposureDelay = Duration.ZERO,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    private val cameraCaptureTask = CameraCaptureTask(camera, cameraRequest, exposureMaxRepeat = 1)
    private val mountMoveState = BooleanArray(3)
    private val elapsedTime = Stopwatch()

    @Volatile private var azimuthError: Angle = 0.0
    @Volatile private var altitudeError: Angle = 0.0
    @Volatile private var totalError: Angle = 0.0
    @Volatile private var azimuthErrorDirection = ""
    @Volatile private var altitudeErrorDirection = ""
    @Volatile private var savedImage: Path? = null
    @Volatile private var noSolutionAttempts = 0

    init {
        cameraCaptureTask.subscribe(this)
    }

    fun handleCameraEvent(event: CameraEvent) {
        if (camera === event.device) {
            cameraCaptureTask.handleCameraEvent(event)
        }
    }

    override fun accept(event: CameraCaptureEvent) {
        if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
            savedImage = event.savePath!!
        }

        sendEvent(TPPAState.SOLVING, event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info(
            "TPPA started. longitude={}, latitude={}, rightAscension={}, declination={}, camera={}, mount={}, request={}",
            longitude.formatSignedDMS(), latitude.formatSignedDMS(), mount?.rightAscension?.formatHMS(), mount?.declination?.formatSignedDMS(),
            camera, mount, request
        )

        elapsedTime.start()

        while (!cancellationToken.isDone) {
            cancellationToken.waitForPause()

            mount?.tracking(true)

            // SLEWING.
            if (mount != null) {
                if (alignment.state in 1..2 && !mountMoveState[alignment.state]) {
                    MountMoveTask(mount, guideRequest).use {
                        sendEvent(TPPAState.SLEWING)
                        it.execute(cancellationToken)
                        mountMoveState[alignment.state] = true
                    }

                    LOG.info("TPPA slewed. rightAscension={}, declination={}", mount.rightAscension.formatHMS(), mount.declination.formatSignedDMS())
                }
            }

            if (cancellationToken.isDone) break

            sendEvent(TPPAState.SOLVING)

            // CAPTURE.
            cameraCaptureTask.execute(cancellationToken)

            if (cancellationToken.isDone || savedImage == null) {
                break
            }

            // ALIGNMENT.
            val radius = if (mount == null) 0.0 else ThreePointPolarAlignment.DEFAULT_RADIUS

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

            if (cancellationToken.isDone) break

            when (result) {
                is ThreePointPolarAlignmentResult.NeedMoreMeasurement -> {
                    noSolutionAttempts = 0
                    sendEvent(TPPAState.SOLVED)
                    continue
                }
                is ThreePointPolarAlignmentResult.NoPlateSolution -> {
                    noSolutionAttempts++

                    sendEvent(TPPAState.FAILED)

                    if (noSolutionAttempts < 10) {
                        continue
                    } else {
                        LOG.error("exhausted all attempts to plate solve")
                        break
                    }
                }
                is ThreePointPolarAlignmentResult.Measured -> {
                    noSolutionAttempts = 0

                    azimuthError = result.azimuth
                    altitudeError = result.altitude

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
            }
        }

        if (request.stopTrackingWhenDone) {
            mount?.tracking(false)
        }

        sendEvent(TPPAState.FINISHED)

        elapsedTime.stop()

        LOG.info("TPPA finished. camera={}, mount={}, request={}", camera, mount, request)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun processCameraCaptureEvent(event: CameraCaptureEvent?): CameraCaptureEvent? {
        return event?.copy(captureElapsedTime = elapsedTime.elapsed)
    }

    private fun sendEvent(state: TPPAState, capture: CameraCaptureEvent? = null) {
        val event = TPPAEvent(
            camera, state,
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

        super.reset()
    }

    override fun close() {
        cameraCaptureTask.close()
        super.close()
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val LOG = loggerFor<TPPATask>()
    }
}
