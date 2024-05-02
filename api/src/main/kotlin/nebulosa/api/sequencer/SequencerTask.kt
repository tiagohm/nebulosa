package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.cameras.CameraStartCaptureRequest
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
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
) : Task<SequencerEvent>(), Consumer<Any> {

    private val usedEntries = plan.entries.filter { it.enabled }

    private val initialDelayTask = DelayTask(plan.initialDelay)

    private val sequencerId = AtomicInteger()
    private val tasks = LinkedList<Task<*>>()
    private val currentTask = AtomicReference<Task<*>>()

    @Volatile private var estimatedCaptureTime = initialDelayTask.duration

    init {
        require(usedEntries.isNotEmpty()) { "no entries found" }

        initialDelayTask.subscribe(this)

        fun mapRequest(request: CameraStartCaptureRequest, exposureAmount: Int = request.exposureAmount): CameraStartCaptureRequest {
            return request.copy(
                savePath = plan.savePath, autoSave = true, autoSubFolderMode = plan.autoSubFolderMode,
                exposureAmount = exposureAmount
            )
        }

        if (plan.captureMode == SequenceCaptureMode.FULLY || usedEntries.size == 1) {
            for (i in usedEntries.indices) {
                val request = mapRequest(usedEntries[i])

                // ID.
                tasks.add(SequencerIdTask(plan.entries.indexOf(usedEntries[i]) + 1))

                // FILTER WHEEL.
                request.wheelMoveTask()?.also(tasks::add)

                // CAPTURE.
                val cameraCaptureTask = CameraCaptureTask(camera, request, guider)
                cameraCaptureTask.subscribe(this)
                estimatedCaptureTime += cameraCaptureTask.estimatedCaptureTime
                tasks.add(cameraCaptureTask)
            }
        } else {
            val sequenceIdTasks = usedEntries.map { SequencerIdTask(plan.entries.indexOf(it) + 1) }
            val requests = usedEntries.map { mapRequest(it, 1) }
            val cameraCaptureTasks = requests.mapIndexed { i, req -> CameraCaptureTask(camera, req, guider, i > 0) }
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
        for (task in tasks) {
            if (cancellationToken.isDone) break
            currentTask.set(task)
            task.execute(cancellationToken)
            currentTask.set(null)
        }
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

                }
            }
        }
    }

    private inner class SequencerIdTask(private val id: Int) : Task<Unit>() {

        override fun execute(cancellationToken: CancellationToken) {
            sequencerId.set(id)
        }
    }
}
