package nebulosa.api.alignment.polar.tppa

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult
import nebulosa.api.cameras.*
import nebulosa.api.message.MessageEvent
import nebulosa.api.mounts.MountEventAware
import nebulosa.api.mounts.MountMoveRequest
import nebulosa.api.mounts.MountMoveTask
import nebulosa.api.mounts.MountTrackTask
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayEvent
import nebulosa.job.manager.delay.DelayStarted
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.formatSignedDMS
import nebulosa.platesolver.PlateSolver
import nebulosa.util.time.Stopwatch
import java.nio.file.Files
import java.time.Duration
import kotlin.math.hypot

data class TPPAJob(
    @JvmField val tppaExecutor: TPPAExecutor,
    @JvmField val camera: Camera,
    @JvmField val solver: PlateSolver,
    @JvmField val request: TPPAStartRequest,
    @JvmField val mount: Mount,
    @JvmField val longitude: Angle = mount.longitude,
    @JvmField val latitude: Angle = mount.latitude,
) : AbstractJob(), CameraEventAware, MountEventAware {

    @JvmField val mountMoveRequest = MountMoveRequest(request.stepDirection, request.stepDuration, request.stepSpeed)

    @JvmField val cameraRequest = request.capture.copy(
        savePath = CAPTURE_SAVE_PATH,
        exposureAmount = 0, exposureDelay = Duration.ZERO,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    @JvmField val status = TPPAEvent(camera)

    @Volatile @JvmField internal var noSolutionAttempts = 0

    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    private val cameraExposureTask = CameraExposureTask(this, camera, cameraRequest)
    private val settleDelayTask = DelayTask(this, SETTLE_TIME)
    private val tppaTask = TPPATask(this, alignment)
    private val mountTrackTask = MountTrackTask(this, mount, true)
    private val mountMoveTask = MountMoveTask(this, mount, mountMoveRequest)
    private val mountMoveState = BooleanArray(3)
    private val stopwatch = Stopwatch()

    inline val savedPath
        get() = status.capture.savedPath

    init {
        add(mountTrackTask)
        add(mountMoveTask)
        add(settleDelayTask)
        add(cameraExposureTask)
        add(tppaTask)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun handleMountEvent(event: MountEvent) {
        mountTrackTask.handleMountEvent(event)
    }

    override fun isLoop(): Boolean {
        return true
    }

    override fun canRun(prev: Task?, current: Task): Boolean {
        if (current === settleDelayTask) {
            return prev === mountMoveTask
        } else if (current === mountMoveTask) {
            return alignment.state.ordinal in 1..2 && !mountMoveState[alignment.state.ordinal]
        }

        return super.canRun(prev, current)
    }

    override fun beforeTask(task: Task) {
        if (task === mountMoveTask) {
            status.state = TPPAState.SLEWING
            status.send()
        } else if (task === cameraExposureTask) {
            status.capture.savedPath = null
            status.state = TPPAState.EXPOSURING
            status.send()
        } else if (task === tppaTask) {
            status.state = TPPAState.SOLVING
            status.send()
        }
    }

    override fun afterTask(task: Task, exception: Throwable?): Boolean {
        if (exception == null) {
            if (task === mountMoveTask) {
                status.rightAscension = task.mount.rightAscension
                status.declination = task.mount.declination
                status.state = TPPAState.SLEWED
                status.send()
            } else if (task === cameraExposureTask) {
                return status.capture.savedPath != null
            }
        }

        return super.afterTask(task, exception)
    }

    override fun onPause(paused: Boolean) {
        if (paused) {
            status.state = TPPAState.PAUSING
            status.send()
        }

        super.onPause(paused)
    }

    override fun beforePause(task: Task) {
        status.state = TPPAState.PAUSED
        status.send()
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                if (event is CameraExposureStarted) {
                    status.capture.captureElapsedTime = stopwatch.elapsedMicroseconds
                }

                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureFinished) {
                    status.capture.send()
                }

                status.send()
            }
            is DelayEvent -> {
                if (event is DelayStarted) {
                    status.capture.captureElapsedTime = stopwatch.elapsedMicroseconds
                }

                status.capture.handleCameraDelayEvent(event)

                if (event.task === settleDelayTask) {
                    status.state = TPPAState.SETTLING
                } else if (event.task === mountMoveTask.delayTask) {
                    status.state = TPPAState.SLEWING
                }

                status.send()
            }
            is ThreePointPolarAlignmentResult.NeedMoreMeasurement -> {
                noSolutionAttempts = 0
                status.rightAscension = event.rightAscension
                status.declination = event.declination
                status.state = TPPAState.SOLVED
                status.send()
            }
            is ThreePointPolarAlignmentResult.NoPlateSolution -> {
                noSolutionAttempts++
                status.state = TPPAState.FAILED

                status.send()

                if (noSolutionAttempts >= MAX_ATTEMPTS) {
                    LOG.error("exhausted all attempts to plate solve")
                    stop()
                }
            }
            is ThreePointPolarAlignmentResult.Measured -> {
                noSolutionAttempts = 0

                status.rightAscension = event.rightAscension
                status.declination = event.declination
                status.azimuthError = event.azimuth
                status.altitudeError = event.altitude
                status.totalError = hypot(status.azimuthError, status.altitudeError)

                status.azimuthErrorDirection = when {
                    status.azimuthError > 0 -> if (latitude > 0) "🠔 Move LEFT/WEST" else "🠔 Move LEFT/EAST"
                    status.azimuthError < 0 -> if (latitude > 0) "Move RIGHT/EAST 🠖" else "Move RIGHT/WEST 🠖"
                    else -> ""
                }

                status.altitudeErrorDirection = when {
                    status.altitudeError > 0 -> if (latitude > 0) "🠗 Move DOWN" else "Move UP 🠕"
                    status.altitudeError < 0 -> if (latitude > 0) "Move UP 🠕" else "🠗 Move DOWN"
                    else -> ""
                }

                LOG.debug(
                    "TPPA aligned. azimuthError={}, altitudeError={}",
                    status.azimuthError.formatSignedDMS(), status.altitudeError.formatSignedDMS()
                )

                status.state = TPPAState.COMPUTED
                status.send()
            }
        }
    }

    override fun beforeStart() {
        LOG.debug("TPPA started. longitude={}, latitude={}, camera={}, mount={}, request={}", longitude, latitude, camera, mount, request)

        status.rightAscension = mount.rightAscension
        status.declination = mount.declination

        stopwatch.start()
    }

    override fun afterFinish() {
        LOG.debug("TPPA finished. camera={}, mount={}, request={}", camera, mount, request)

        stopwatch.stop()

        if (request.stopTrackingWhenDone) {
            mount.tracking(false)
        }

        status.state = TPPAState.FINISHED
        status.send()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        tppaExecutor.accept(this)
    }

    companion object {

        const val MAX_ATTEMPTS = 30

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val SETTLE_TIME = Duration.ofSeconds(5)
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("tppa-")
        @JvmStatic private val LOG = loggerFor<TPPAJob>()
    }
}
