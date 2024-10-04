package nebulosa.api.sequencer

import nebulosa.api.calibration.CalibrationFrameProvider
import nebulosa.api.cameras.*
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.guiding.DitherAfterExposureEvent
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.message.MessageEvent
import nebulosa.api.rotators.RotatorEventAware
import nebulosa.api.wheels.WheelEventAware
import nebulosa.api.wheels.WheelMoveTask
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorEvent
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.SplitTask
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayEvent
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.nio.file.Path

data class SequencerJob(
    @JvmField val sequencerExecutor: SequencerExecutor,
    @JvmField val camera: Camera,
    @JvmField val plan: SequencerPlanRequest,
    @JvmField val guider: Guider? = null,
    @JvmField val mount: Mount? = null,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
    @JvmField val rotator: Rotator? = null,
    private val calibrationFrameProvider: CalibrationFrameProvider? = null,
) : AbstractJob(), CameraEventAware, WheelEventAware, FocuserEventAware, RotatorEventAware {

    private val sequences = plan.sequences.filter { it.enabled }
    private val initialDelayTask = DelayTask(this, plan.initialDelay)
    private val waitForSettleTask = WaitForSettleTask(this, guider)
    private val liveStackingManager = CameraLiveStackingManager(calibrationFrameProvider)

    @Volatile private var estimatedCaptureTime = initialDelayTask.durationInMilliseconds * 1000L
    @Volatile private var captureStartElapsedTime = 0L

    @JvmField val status = SequencerEvent(camera)

    init {
        require(sequences.isNotEmpty()) { "no entries found" }

        add(initialDelayTask)

        if (plan.captureMode == SequencerCaptureMode.FULLY || sequences.size == 1) {
            var first = true

            for (i in sequences.indices) {
                val request = sequences[i].map()

                // ID.
                add(SequencerIdTask(plan.sequences.indexOfFirst { it === sequences[i] } + 1))

                // FILTER WHEEL.
                request.wheelMoveTask()?.also(::add)

                // DELAY.
                val delayTask = DelayTask(this, request.exposureDelay)


                // CAPTURE.
                val cameraCaptureTask = CameraExposureTask(this, camera, request)

                repeat(request.exposureAmount) {
                    if (!first) {
                        add(SplitTask(listOf(delayTask, waitForSettleTask), sequencerExecutor))
                    }

                    add(cameraCaptureTask)
                    first = false

                    estimatedCaptureTime += cameraCaptureTask.exposureTimeInMicroseconds
                    estimatedCaptureTime += delayTask.durationInMilliseconds * 1000L
                }

                // DITHER.
                add(DitherAfterExposureTask(this, guider, request.dither))
            }
        } else {
            val requests = sequences.map { it.map() }
            val sequenceIdTasks = sequences.map { req -> SequencerIdTask(plan.sequences.indexOfFirst { it === req } + 1) }
            val wheelMoveTasks = requests.map { it.wheelMoveTask() }
            val cameraExposureTasks = requests.map { CameraExposureTask(this, camera, it) }
            val delayTasks = requests.map { DelayTask(this, it.exposureDelay) }
            val ditherAfterExposureTask = requests.map { DitherAfterExposureTask(this, guider, it.dither) }
            val delayAndWaitForSettleSplitTasks = delayTasks.map { SplitTask(listOf(it, waitForSettleTask), sequencerExecutor) }
            val count = IntArray(requests.size) { requests[it].exposureAmount }
            var first = true

            while (count.sum() > 0) {
                for (i in count.indices) {
                    if (count[i] > 0) {
                        // ID.
                        add(sequenceIdTasks[i])

                        // FILTER WHEEL.
                        wheelMoveTasks[i]?.also(::add)

                        // DELAY.
                        if (!first) {
                            add(delayAndWaitForSettleSplitTasks[i])
                        }

                        estimatedCaptureTime += delayTasks[i].durationInMilliseconds * 1000L

                        // CAPTURE.
                        add(cameraExposureTasks[i])
                        estimatedCaptureTime += cameraExposureTasks[i].exposureTimeInMicroseconds

                        // DITHER.
                        add(ditherAfterExposureTask[i])

                        count[i]--
                        first = false
                    }
                }
            }
        }
    }

    override fun handleCameraEvent(event: CameraEvent) {
        (currentTask as? CameraEventAware)?.handleCameraEvent(event)
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        (currentTask as? WheelEventAware)?.handleFilterWheelEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) = Unit

    override fun handleRotatorEvent(event: RotatorEvent) = Unit

    private fun CameraStartCaptureRequest.map() = copy(
        savePath = plan.savePath,
        autoSave = true,
        autoSubFolderMode = plan.autoSubFolderMode,
        dither = plan.dither,
        liveStacking = plan.liveStacking,
        namingFormat = plan.namingFormat,
    )

    private fun CameraStartCaptureRequest.wheelMoveTask(): WheelMoveTask? {
        if (wheel != null) {
            val filterPosition = if (frameType == FrameType.DARK) shutterPosition else filterPosition

            if (filterPosition in 1..wheel.count) {
                return WheelMoveTask(this@SequencerJob, wheel, filterPosition)
            }
        }

        return null
    }

    private fun addFrameToLiveStacker(request: CameraStartCaptureRequest, path: Path?): Path? {
        return if (path != null && liveStackingManager.start(request, path)) {
            try {
                status.capture.state = CameraCaptureState.STACKING
                status.send()

                liveStackingManager.stack(request, path)
            } catch (_: Throwable) {
                null
            }
        } else {
            null
        }
    }

    override fun onPause(paused: Boolean) {
        if (paused) {
            status.state = SequencerState.PAUSING
            status.send()
        }

        super.onPause(paused)
    }

    override fun beforePause(task: Task) {
        status.state = SequencerState.PAUSED
        status.send()
    }

    override fun afterPause(task: Task) {
        status.state = SequencerState.RUNNING
        status.send()
    }

    override fun canRun(prev: Task?, current: Task): Boolean {
        if (current is DitherAfterExposureTask) {
            return !isCancelled && guider != null
                    && status.capture.exposureCount >= 1 && current.request.afterExposures > 0
                    && status.capture.exposureCount % current.request.afterExposures == 0
        }

        return super.canRun(prev, current)
    }

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                status.capture.handleCameraDelayEvent(event)
                status.elapsedTime += event.waitTime
                status.send()
            }
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureStarted) {
                    captureStartElapsedTime = status.elapsedTime
                } else {
                    status.elapsedTime = captureStartElapsedTime + event.elapsedTime

                    if (event is CameraExposureFinished) {
                        status.capture.liveStackedPath = addFrameToLiveStacker(event.task.request, status.capture.savedPath)
                        status.capture.send()
                    }
                }

                status.remainingTime = if (estimatedCaptureTime > status.elapsedTime) estimatedCaptureTime - status.elapsedTime else 0L
                if (status.elapsedTime > 0L) status.progress = status.elapsedTime.toDouble() / (status.elapsedTime + status.remainingTime)
                status.send()
            }
            is DitherAfterExposureEvent -> {
                status.capture.state = CameraCaptureState.DITHERING
                status.send()
            }
        }
    }

    override fun beforeStart() {
        LOG.debug { "Sequencer started. camera=$camera, mount=$mount, wheel=$wheel, focuser=$focuser, rotator=$rotator, plan=$plan" }

        status.state = SequencerState.RUNNING
        status.send()
    }

    override fun afterFinish() {
        liveStackingManager.close()

        status.state = SequencerState.IDLE
        status.send()

        LOG.debug { "Sequencer finished. camera=$camera, mount=$mount, wheel=$wheel, focuser=$focuser, rotator=$rotator, plan=$plan" }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        sequencerExecutor.accept(this)
    }

    private inner class SequencerIdTask(private val id: Int) : Task {

        override fun run() {
            LOG.debug { "Sequence in execution. id=$id" }
            status.id = id
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SequencerJob>()
    }
}
