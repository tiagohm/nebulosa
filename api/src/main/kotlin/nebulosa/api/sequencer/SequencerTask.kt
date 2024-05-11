package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureState
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.messages.MessageEvent
import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.api.wheels.WheelMoveTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

// https://cdn.diffractionlimited.com/help/maximdl/Autosave_Sequence.htm
// https://nighttime-imaging.eu/docs/master/site/tabs/sequence/
// https://nighttime-imaging.eu/docs/master/site/sequencer/advanced/advanced/

data class SequencerTask(
    @JvmField val camera: Camera,
    @JvmField val plan: SequencePlanRequest,
    @JvmField val guider: Guider? = null,
    @JvmField val mount: Mount? = null,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
) : Task<MessageEvent>(), Consumer<Any> {

    private val usedEntries = plan.entries.filter { it.enabled }

    private val initialDelayTask = DelayTask(plan.initialDelay)

    private val sequencerId = AtomicInteger()
    private val tasks = LinkedList<Task<*>>()
    private val currentTask = AtomicReference<Task<*>>()

    @Volatile private var estimatedCaptureTime = initialDelayTask.duration

    @Volatile private var elapsedTime = Duration.ZERO
    @Volatile private var prevElapsedTime = Duration.ZERO
    @Volatile private var remainingTime = Duration.ZERO
    @Volatile private var progress = 0.0

    init {
        require(usedEntries.isNotEmpty()) { "no entries found" }

        initialDelayTask.subscribe(this)
        tasks.add(initialDelayTask)

        fun mapRequest(request: CameraStartCaptureRequest): CameraStartCaptureRequest {
            return request.copy(savePath = plan.savePath, autoSave = true, autoSubFolderMode = plan.autoSubFolderMode)
        }

        if (plan.captureMode == SequenceCaptureMode.FULLY || usedEntries.size == 1) {
            for (i in usedEntries.indices) {
                val request = mapRequest(usedEntries[i])

                // ID.
                tasks.add(SequencerIdTask(plan.entries.indexOfFirst { it === usedEntries[i] } + 1))

                // FILTER WHEEL.
                request.wheelMoveTask()?.also(tasks::add)

                // CAPTURE.
                val cameraCaptureTask = CameraCaptureTask(camera, request, guider)
                cameraCaptureTask.subscribe(this)
                estimatedCaptureTime += cameraCaptureTask.estimatedCaptureTime
                tasks.add(cameraCaptureTask)
            }
        } else {
            val sequenceIdTasks = usedEntries.map { req -> SequencerIdTask(plan.entries.indexOfFirst { it === req } + 1) }
            val requests = usedEntries.map { mapRequest(it) }
            val cameraCaptureTasks = requests.mapIndexed { i, req -> CameraCaptureTask(camera, req, guider, i > 0, 1) }
            val wheelMoveTasks = requests.map { it.wheelMoveTask() }
            val count = IntArray(requests.size) { usedEntries[it].exposureAmount }

            for (task in cameraCaptureTasks) {
                task.subscribe(this)
                estimatedCaptureTime += task.estimatedCaptureTime
            }

            while (count.sum() > 0) {
                for (i in usedEntries.indices) {
                    if (count[i] > 0) {
                        count[i]--

                        tasks.add(sequenceIdTasks[i])
                        wheelMoveTasks[i]?.also(tasks::add)
                        tasks.add(cameraCaptureTasks[i])
                    }
                }
            }
        }
    }

    fun handleCameraEvent(event: CameraEvent) {
        val task = currentTask.get()

        if (task is CameraCaptureTask) {
            task.handleCameraEvent(event)
        }
    }

    fun handleFilterWheelEvent(event: FilterWheelEvent) {
        val task = currentTask.get()

        if (task is WheelMoveTask) {
            task.handleFilterWheelEvent(event)
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("Sequencer started. camera={}, mount={}, wheel={}, focuser={}, plan={}", camera, mount, wheel, focuser, plan)

        camera.snoop(listOf(mount, wheel, focuser))

        for (task in tasks) {
            if (cancellationToken.isDone) break
            currentTask.set(task)
            task.execute(cancellationToken)
            currentTask.set(null)
        }

        if (remainingTime.toMillis() > 0L) {
            remainingTime = Duration.ZERO
            progress = 1.0
            sendEvent()
        }

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

    override fun close() {
        tasks.forEach { it.close() }
    }

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                if (event.task === initialDelayTask) {
                    elapsedTime += event.waitTime
                    computeRemainingTimeAndProgress()
                    sendEvent()
                }
            }
            is CameraCaptureEvent -> {
                when (event.state) {
                    CameraCaptureState.CAPTURE_STARTED -> {
                        prevElapsedTime = elapsedTime
                    }
                    CameraCaptureState.EXPOSURING, CameraCaptureState.WAITING -> {
                        elapsedTime = prevElapsedTime + event.captureElapsedTime
                        computeRemainingTimeAndProgress()
                    }
                    CameraCaptureState.EXPOSURE_FINISHED -> {
                        onNext(event)
                    }
                    else -> Unit
                }

                sendEvent(event)
            }
        }
    }

    private fun computeRemainingTimeAndProgress() {
        remainingTime = if (estimatedCaptureTime > elapsedTime) estimatedCaptureTime - elapsedTime else Duration.ZERO
        progress = (estimatedCaptureTime - remainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
    }

    private fun sendEvent(capture: CameraCaptureEvent? = null) {
        onNext(SequencerEvent(sequencerId.get(), elapsedTime, remainingTime, progress, capture))
    }

    private inner class SequencerIdTask(private val id: Int) : Task<Unit>() {

        override fun execute(cancellationToken: CancellationToken) {
            LOG.info("Sequence started. id={}", id)
            sequencerId.set(id)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SequencerTask>()
    }
}