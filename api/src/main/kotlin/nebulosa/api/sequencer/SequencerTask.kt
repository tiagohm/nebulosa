package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.calibration.CalibrationFrameProvider
import nebulosa.api.cameras.*
import nebulosa.api.message.MessageEvent
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.api.wheels.WheelEventAware
import nebulosa.api.wheels.WheelMoveTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.PauseListener
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.log.loggerFor
import java.time.Duration
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

// https://cdn.diffractionlimited.com/help/maximdl/Autosave_Sequence.htm
// https://nighttime-imaging.eu/docs/master/site/tabs/sequence/
// https://nighttime-imaging.eu/docs/master/site/sequencer/advanced/advanced/

data class SequencerTask(
    @JvmField val camera: Camera,
    @JvmField val plan: SequencerPlanRequest,
    @JvmField val guider: Guider? = null,
    @JvmField val mount: Mount? = null,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
    @JvmField val rotator: Rotator? = null,
    private val executor: Executor? = null,
    private val calibrationFrameProvider: CalibrationFrameProvider? = null,
) : AbstractTask<MessageEvent>(), Consumer<Any>, CameraEventAware, WheelEventAware, PauseListener {

    private val sequences = plan.sequences.filter { it.enabled }

    private val initialDelayTask = DelayTask(plan.initialDelay)

    private val sequencerId = AtomicInteger()
    private val tasks = LinkedList<Task>()
    private val currentTask = AtomicReference<Task>()
    private val pausing = AtomicBoolean()
    private val paused = AtomicBoolean()

    @Volatile private var estimatedCaptureTime = initialDelayTask.duration

    @Volatile private var elapsedTime = Duration.ZERO
    @Volatile private var prevElapsedTime = Duration.ZERO
    @Volatile private var remainingTime = Duration.ZERO
    @Volatile private var progress = 0.0

    init {
        require(sequences.isNotEmpty()) { "no entries found" }

        initialDelayTask.subscribe(this)
        tasks.add(initialDelayTask)

        fun mapRequest(request: CameraStartCaptureRequest) = request.copy(
            savePath = plan.savePath, autoSave = true,
            autoSubFolderMode = plan.autoSubFolderMode,
            dither = plan.dither,
            namingFormat = plan.namingFormat,
        )

        if (plan.captureMode == SequencerCaptureMode.FULLY || sequences.size == 1) {
            for (i in sequences.indices) {
                val request = mapRequest(sequences[i])

                // ID.
                tasks.add(SequencerIdTask(plan.sequences.indexOfFirst { it === sequences[i] } + 1))

                // FILTER WHEEL.
                request.wheelMoveTask()?.also(tasks::add)

                // CAPTURE.
                val cameraCaptureTask = CameraCaptureTask(
                    camera, request, guider, false, executor,
                    calibrationFrameProvider,
                    mount, wheel, focuser, rotator
                )

                cameraCaptureTask.subscribe(this)
                estimatedCaptureTime += cameraCaptureTask.estimatedCaptureTime
                tasks.add(SequenceCaptureModeCameraCaptureTask(cameraCaptureTask, SequencerCaptureMode.FULLY, i))
            }
        } else {
            val sequenceIdTasks = sequences.map { req -> SequencerIdTask(plan.sequences.indexOfFirst { it === req } + 1) }
            val requests = sequences.map { mapRequest(it) }
            val cameraCaptureTasks = requests.mapIndexed { i, req ->
                val task = CameraCaptureTask(
                    camera, req, guider,
                    i > 0, executor, calibrationFrameProvider,
                    mount, wheel, focuser, rotator
                )

                SequenceCaptureModeCameraCaptureTask(task, SequencerCaptureMode.INTERLEAVED, i)
            }
            val wheelMoveTasks = requests.map { it.wheelMoveTask() }
            val count = IntArray(requests.size) { sequences[it].exposureAmount }

            for ((cameraCaptureTask) in cameraCaptureTasks) {
                cameraCaptureTask.subscribe(this)
                estimatedCaptureTime += cameraCaptureTask.estimatedCaptureTime
            }

            while (count.sum() > 0) {
                for (i in sequences.indices) {
                    if (count[i] > 0) {
                        tasks.add(sequenceIdTasks[i])
                        wheelMoveTasks[i]?.also(tasks::add)

                        val task = cameraCaptureTasks[i]

                        if (count[i] == sequences[i].exposureAmount) {
                            tasks.add(InitializeCameraCaptureTask(task.task))
                        }

                        tasks.add(task)

                        count[i]--

                        if (count[i] == 0) {
                            tasks.add(FininalizeCameraCaptureTask(task.task))
                        }
                    }
                }
            }
        }
    }

    override fun handleCameraEvent(event: CameraEvent) {
        when (val task = currentTask.get()) {
            is CameraCaptureTask -> task.handleCameraEvent(event)
            is SequenceCaptureModeCameraCaptureTask -> task.task.handleCameraEvent(event)
        }
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        when (val task = currentTask.get()) {
            is WheelMoveTask -> task.handleFilterWheelEvent(event)
            is CameraCaptureTask -> task.handleFilterWheelEvent(event)
            is SequenceCaptureModeCameraCaptureTask -> task.task.handleFilterWheelEvent(event)
        }
    }

    override fun onPause(paused: Boolean) {
        pausing.set(paused)

        if (paused) {
            sendEvent(SequencerState.PAUSING)
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("Sequencer started. camera={}, mount={}, wheel={}, focuser={}, plan={}", camera, mount, wheel, focuser, plan)

        cancellationToken.listenToPause(this)

        for (task in tasks) {
            if (cancellationToken.isCancelled) break

            currentTask.set(task)
            task.execute(cancellationToken)
            currentTask.set(null)
        }

        if (remainingTime.toMillis() > 0L) {
            remainingTime = Duration.ZERO
            progress = 1.0
            sendEvent(SequencerState.IDLE)
        }

        cancellationToken.unlistenToPause(this)

        LOG.info("Sequencer finished. camera={}, mount={}, wheel={}, focuser={}, plan={}", camera, mount, wheel, focuser, plan)
    }

    private fun CameraStartCaptureRequest.wheelMoveTask(): WheelMoveTask? {
        if (wheel != null) {
            val filterPosition = if (frameType == FrameType.DARK) shutterPosition else filterPosition

            if (filterPosition in 1..wheel.count) {
                return WheelMoveTask(wheel, filterPosition)
            }
        }

        return null
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is SequencerEvent

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                if (event.task === initialDelayTask) {
                    elapsedTime += event.waitTime
                    computeRemainingTimeAndProgress()
                    sendEvent(SequencerState.RUNNING)
                }
            }
            is CameraCaptureEvent -> {
                pausing.set(event.state == CameraCaptureState.PAUSING)
                paused.set(event.state == CameraCaptureState.PAUSED)

                when (event.state) {
                    CameraCaptureState.CAPTURE_STARTED -> {
                        prevElapsedTime = elapsedTime
                    }
                    CameraCaptureState.EXPOSURING,
                    CameraCaptureState.WAITING -> {
                        elapsedTime = prevElapsedTime + event.captureElapsedTime
                        computeRemainingTimeAndProgress()
                    }
                    CameraCaptureState.EXPOSURE_FINISHED -> {
                        onNext(event)
                    }
                    else -> Unit
                }

                sendEvent(SequencerState.RUNNING, event)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun computeRemainingTimeAndProgress() {
        remainingTime = if (estimatedCaptureTime > elapsedTime) estimatedCaptureTime - elapsedTime else Duration.ZERO
        progress = (estimatedCaptureTime - remainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendEvent(state: SequencerState, capture: CameraCaptureEvent? = null) {
        onNext(
            SequencerEvent(
                sequencerId.get(), elapsedTime, remainingTime, progress, capture,
                if (pausing.get()) SequencerState.PAUSING else if (paused.get()) SequencerState.PAUSED else state
            )
        )
    }

    override fun close() {
        tasks.forEach { it.close() }
    }

    private inner class SequencerIdTask(private val id: Int) : Task {

        override fun execute(cancellationToken: CancellationToken) {
            LOG.info("Sequence in execution. id={}", id)
            sequencerId.set(id)
        }
    }

    private data class InitializeCameraCaptureTask(@JvmField val task: CameraCaptureTask) : Task {

        override fun execute(cancellationToken: CancellationToken) {
            task.initialize(cancellationToken)
        }
    }

    private data class SequenceCaptureModeCameraCaptureTask(
        @JvmField val task: CameraCaptureTask,
        @JvmField val mode: SequencerCaptureMode,
        @JvmField val index: Int,
    ) : Task {

        override fun execute(cancellationToken: CancellationToken) {
            if (mode == SequencerCaptureMode.FULLY) {
                task.initialize(cancellationToken)
                task.executeInLoop(cancellationToken)
                task.finalize(cancellationToken)
            } else {
                task.executeOnce(cancellationToken)
            }
        }
    }

    private data class FininalizeCameraCaptureTask(@JvmField val task: CameraCaptureTask) : Task {

        override fun execute(cancellationToken: CancellationToken) {
            task.finalize(cancellationToken)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SequencerTask>()
    }
}
