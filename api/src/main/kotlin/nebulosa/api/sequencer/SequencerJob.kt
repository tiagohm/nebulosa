package nebulosa.api.sequencer

import nebulosa.api.cameras.*
import nebulosa.api.focusers.BacklashCompensationFocuserMoveTask
import nebulosa.api.focusers.BacklashCompensator
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.focusers.FocuserTask
import nebulosa.api.guiding.DitherAfterExposureEvent
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.message.MessageEvent
import nebulosa.api.rotators.RotatorEventAware
import nebulosa.api.rotators.RotatorMoveTask
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
import nebulosa.log.d
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
    private val liveStackingManager: CameraLiveStackingManager? = null,
) : AbstractJob(), CameraEventAware, WheelEventAware, FocuserEventAware, RotatorEventAware {

    private val sequences = plan.sequences.filter { it.enabled }
    private val initialDelayTask = DelayTask(this, plan.initialDelay)
    private val waitForSettleTask = WaitForSettleTask(this, guider)
    private val cameraCaptureEvents = Array(plan.sequences.size + 1) { CameraCaptureEvent(camera, exposureAmount = plan.sequences.getOrNull(it - 1)?.exposureAmount ?: 0) }
    private val backlashCompensator = BacklashCompensator(plan.backlashCompensation, focuser?.maxPosition ?: 0)

    @Volatile private var initialFocuserPosition = 0
    @Volatile private var estimatedCaptureTime = initialDelayTask.duration * 1000L
    @Volatile private var captureStartElapsedTime = 0L

    @JvmField val status = SequencerEvent(camera)

    init {
        require(sequences.isNotEmpty()) { "no entries found" }

        initialFocuserPosition = focuser?.position ?: 0

        add(initialDelayTask)

        if (plan.captureMode == SequencerCaptureMode.FULLY || sequences.size == 1) {
            var first = true

            for (i in sequences.indices) {
                val request = sequences[i].map()

                val id = plan.sequences.indexOfFirst { it === sequences[i] } + 1

                // SEQUENCE ID.
                add(SequencerIdTask(id))

                // FILTER WHEEL.
                if (request.wheelMoveTask()?.also(::add) != null) {
                    // FOCUSER.
                    request.focuserMoveTask()?.also(::add)
                }

                // ROTATOR.
                request.rotatorMoveTask()?.also(::add)

                // DELAY.
                val delayTask = DelayTask(this, request.exposureDelay)

                // CAPTURE.
                val cameraCaptureTask = CameraExposureTask(this, camera, request)

                repeat(request.exposureAmount) {
                    if (!first) {
                        add(SplitTask(listOf(delayTask, waitForSettleTask), sequencerExecutor))
                        cameraCaptureEvents[id].captureRemainingTime += delayTask.duration * 1000L
                    }

                    add(cameraCaptureTask)
                    first = false

                    cameraCaptureEvents[id].captureRemainingTime += cameraCaptureTask.exposureTimeInMicroseconds
                }

                // DITHER.
                add(DitherAfterExposureTask(this, guider, request.dither))
            }
        } else {
            val requests = sequences.map { it.map() }
            val sequenceIdTasks = sequences.map { req -> SequencerIdTask(plan.sequences.indexOfFirst { it === req } + 1) }
            val wheelMoveTasks = requests.map { it.wheelMoveTask() }
            val focuserMoveTasks = requests.map { it.focuserMoveTask() }
            val rotatorMoveTasks = requests.map { it.rotatorMoveTask() }
            val cameraExposureTasks = requests.map { CameraExposureTask(this, camera, it) }
            val delayTasks = requests.map { DelayTask(this, it.exposureDelay) }
            val ditherAfterExposureTask = requests.map { DitherAfterExposureTask(this, guider, it.dither) }
            val delayAndWaitForSettleSplitTasks = delayTasks.map { SplitTask(listOf(it, waitForSettleTask), sequencerExecutor) }
            val count = IntArray(requests.size) { requests[it].exposureAmount }
            var first = true

            while (count.sum() > 0) {
                for (i in count.indices) {
                    if (count[i] > 0) {
                        val id = sequenceIdTasks[i].id

                        // SEQUENCE ID.
                        add(sequenceIdTasks[i])

                        // FILTER WHEEL.
                        if (wheelMoveTasks[i]?.also(::add) != null) {
                            // FOCUSER.
                            focuserMoveTasks[i]?.also(::add)
                        }

                        // ROTATOR.
                        rotatorMoveTasks[i]?.also(::add)

                        // DELAY.
                        if (!first) {
                            add(delayAndWaitForSettleSplitTasks[i])
                            cameraCaptureEvents[id].captureRemainingTime += delayTasks[i].duration * 1000L
                        }

                        // CAPTURE.
                        add(cameraExposureTasks[i])
                        cameraCaptureEvents[id].captureRemainingTime += cameraExposureTasks[i].exposureTimeInMicroseconds

                        // DITHER.
                        add(ditherAfterExposureTask[i])

                        count[i]--
                        first = false
                    }
                }
            }
        }

        estimatedCaptureTime += cameraCaptureEvents.sumOf { it.captureRemainingTime }
    }

    override fun handleCameraEvent(event: CameraEvent) {
        (currentTask as? CameraEventAware)?.handleCameraEvent(event)
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        (currentTask as? WheelEventAware)?.handleFilterWheelEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        (currentTask as? FocuserEventAware)?.handleFocuserEvent(event)
    }

    override fun handleRotatorEvent(event: RotatorEvent) {
        (currentTask as? RotatorEventAware)?.handleRotatorEvent(event)
    }

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
            val position = if (frameType == FrameType.DARK) shutterPosition else filterPosition

            if (position in 1..wheel.count) {
                return WheelMoveTask(this@SequencerJob, wheel, position)
            }
        }

        return null
    }

    private fun CameraStartCaptureRequest.focuserMoveTask(): FocuserTask? {
        if (focuser != null && focusOffset != 0 && frameType != FrameType.DARK) {
            val position = initialFocuserPosition + focusOffset

            if (position in 0..focuser.maxPosition) {
                return BacklashCompensationFocuserMoveTask(this@SequencerJob, focuser, position, backlashCompensator)
            }
        }

        return null
    }

    private fun CameraStartCaptureRequest.rotatorMoveTask(): RotatorMoveTask? {
        if (rotator != null && angle.isFinite() && frameType != FrameType.DARK && angle in 0.0..rotator.maxAngle) {
            return RotatorMoveTask(this@SequencerJob, rotator, angle)
        }

        return null
    }

    private fun addFrameToLiveStacker(request: CameraStartCaptureRequest, path: Path?): Path? {
        return if (path != null && liveStackingManager != null && liveStackingManager.start(request, path)) {
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
                if (event.task !== initialDelayTask) {
                    status.capture.handleCameraDelayEvent(event)
                }

                status.elapsedTime += event.waitTime
                status.computeRemainingTimeAndProgress()
                status.send()
            }
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureFailed) {
                    return stop()
                }

                if (event is CameraExposureStarted) {
                    captureStartElapsedTime = status.elapsedTime
                } else {
                    status.elapsedTime = captureStartElapsedTime + event.elapsedTime

                    if (event is CameraExposureFinished) {
                        status.capture.liveStackedPath = addFrameToLiveStacker(event.task.request, status.capture.savedPath)
                        status.capture.send()
                    }
                }

                status.computeRemainingTimeAndProgress()
                status.send()
            }
            is DitherAfterExposureEvent -> {
                status.capture.state = CameraCaptureState.DITHERING
                status.send()
            }
        }
    }

    override fun beforeStart() {
        LOG.d { debug("Sequencer started. camera={}, mount={}, wheel={}, focuser={}, rotator={}, plan={}", camera, mount, wheel, focuser, rotator, plan) }

        camera.snoop(listOf(mount, wheel, focuser, rotator))

        status.state = SequencerState.RUNNING
        status.send()
    }

    override fun beforeTask(task: Task) {
        if (task === initialDelayTask && initialDelayTask.duration > 0L) {
            status.state = SequencerState.WAITING
        }
    }

    override fun afterTask(task: Task, exception: Throwable?): Boolean {
        if (exception == null) {
            if (task is SequencerIdTask) {
                status.capture = cameraCaptureEvents[task.id]
            } else if (task === initialDelayTask) {
                status.state = SequencerState.RUNNING
            }
        }

        return super.afterTask(task, exception)
    }

    override fun afterFinish() {
        liveStackingManager?.close()

        status.state = SequencerState.IDLE
        status.capture.state = CameraCaptureState.IDLE
        status.send()

        LOG.d { debug("Sequencer finished. camera={}, mount={}, wheel={}, focuser={}, rotator={}, plan={}", camera, mount, wheel, focuser, rotator, plan) }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun SequencerEvent.computeRemainingTimeAndProgress() {
        remainingTime = if (estimatedCaptureTime > elapsedTime) estimatedCaptureTime - elapsedTime else 0L
        progress = elapsedTime.toDouble() / estimatedCaptureTime
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        sequencerExecutor.accept(this)
    }

    private inner class SequencerIdTask(@JvmField val id: Int) : Task {

        override fun run() {
            LOG.d { debug("Sequence in execution. id={}", id) }
            status.id = id
        }
    }

    companion object {

        private val LOG = loggerFor<SequencerJob>()
    }
}
