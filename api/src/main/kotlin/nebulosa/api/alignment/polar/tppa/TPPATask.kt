package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraExposureEvent
import nebulosa.api.cameras.CameraExposureTask
import nebulosa.api.mounts.MountMoveTask
import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
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
) : Task<TPPAEvent>(), Consumer<Any> {

    @JvmField val cameraRequest = request.capture.copy(
        savePath = Files.createTempDirectory("tppa"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    private val cameraExposureTask = CameraExposureTask(camera, cameraRequest)
    private val stepDistances = DoubleArray(2) { if (request.eastDirection) request.stepDistance else -request.stepDistance }

    @Volatile private var stepCount = 0
    @Volatile private var elapsedTime: Duration = Duration.ZERO
    @Volatile private var rightAscension: Angle = 0.0
    @Volatile private var declination: Angle = 0.0
    @Volatile private var azimuthError: Angle = 0.0
    @Volatile private var altitudeError: Angle = 0.0
    @Volatile private var totalError: Angle = 0.0
    @Volatile private var azimuthErrorDirection = ""
    @Volatile private var altitudeErrorDirection = ""
    @Volatile private var savedImage: Path? = null
    @Volatile private var noSolutionAttempts = 0
    @Volatile private var mountMoveTask: MountMoveTask? = null

    init {
        cameraExposureTask.subscribe(this)
    }

    fun handleCameraEvent(event: CameraEvent) {
        if (camera === event.device) {
            cameraExposureTask.handleCameraEvent(event)
        }
    }

    fun handleMountEvent(event: MountEvent) {
        if (mount === event.device) {
            mountMoveTask?.handleMountEvent(event)
        }
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                savedImage = event.savedPath ?: return
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("TPPA started. camera={}, mount={}, request={}", camera, mount, request)

        while (!cancellationToken.isDone) {
            mount?.tracking(true)

            if (cancellationToken.isPaused) {
                sendEvent(TPPAState.PAUSED)
                cancellationToken.waitForPause()
            }

            if (cancellationToken.isDone) return

            // SLEWING.
            if (mount != null) {
                if (alignment.state in 1..2 && stepDistances[alignment.state - 1] != 0.0) {
                    mountMoveTask?.close()

                    MountMoveTask(mount, mount.rightAscension + stepDistances[alignment.state - 1].deg, mount.declination).use {
                        mountMoveTask = it
                        rightAscension = it.rightAscension
                        declination = it.declination
                        sendEvent(TPPAState.SLEWING)
                        it.execute(cancellationToken)
                        stepDistances[alignment.state - 1] = 0.0
                    }
                }

                if (cancellationToken.isDone) return
            }

            sendEvent(TPPAState.SOLVING)

            // CAPTURE.
            cameraExposureTask.execute(cancellationToken)

            // ALIGNMENT.
            val radius = if (mount == null) 0.0 else ThreePointPolarAlignment.DEFAULT_RADIUS

            val result = alignment.align(
                savedImage!!, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius,
                request.compensateRefraction, cancellationToken
            )

            LOG.info("TPPA alignment completed. result=$result")

            if (cancellationToken.isDone) return

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

                    if (noSolutionAttempts < 10) {
                        sendEvent(TPPAState.FAILED)
                        continue
                    } else {
                        LOG.error("exhausted all attempts to plate solve")
                        return
                    }
                }
                is ThreePointPolarAlignmentResult.Measured -> {
                    noSolutionAttempts = 0

                    rightAscension = result.rightAscension
                    declination = result.declination
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
                        rightAscension.formatHMS(), declination.formatSignedDMS(), azimuthError.formatSignedDMS(), altitudeError.formatSignedDMS(),
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

        LOG.info("TPPA finished. camera={}, mount={}, request={}", camera, mount, request)
    }

    private fun sendEvent(state: TPPAState) {
        val event = TPPAEvent(
            camera, mount,
            state, stepCount, elapsedTime, rightAscension, declination,
            azimuthError, altitudeError, totalError, azimuthErrorDirection, altitudeErrorDirection
        )

        onNext(event)
    }

    override fun close() {
        cameraExposureTask.close()
        mountMoveTask?.close()
        super.close()
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME: Duration = Duration.ofSeconds(1L)
        @JvmStatic private val LOG = loggerFor<TPPATask>()
    }
}
