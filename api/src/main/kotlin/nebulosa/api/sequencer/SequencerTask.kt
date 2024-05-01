package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import java.util.concurrent.atomic.AtomicInteger

data class SequencerTask(
    @JvmField val camera: Camera,
    @JvmField val plan: SequencePlanRequest,
    @JvmField val guider: Guider,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
) : Task<SequencerEvent>(), Consumer<Any> {

    private val usedEntries = plan.entries.filter { it.enabled }

    private val initialDelayTask = DelayTask(plan.initialDelay)

    private val sequencerId = AtomicInteger()
    private val tasks = ArrayList<Task<*>>()

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
                // ID.
                tasks.add(SequencerIdTask(plan.entries.indexOf(usedEntries[i]) + 1))

                // CAPTURE.
                val request = mapRequest(usedEntries[i])
                val cameraCaptureTask = CameraCaptureTask(camera, request)
                cameraCaptureTask.subscribe(this)
                estimatedCaptureTime += cameraCaptureTask.estimatedCaptureTime
                tasks.add(cameraCaptureTask)
            }
        } else {
            val sequenceIdTasks = usedEntries.map { SequencerIdTask(plan.entries.indexOf(it) + 1) }
            val requests = usedEntries.map { mapRequest(it, 1) }
            val cameraCaptureTasks = requests.mapIndexed { i, req -> CameraCaptureTask(camera, req, delayOnFirstExposure = i > 0) }
            val count = IntArray(requests.size) { usedEntries[it].exposureAmount }

            cameraCaptureTasks.forEach {
                it.subscribe(this)
                estimatedCaptureTime += it.estimatedCaptureTime
            }

            while (count.sum() > 0) {
                for (i in usedEntries.indices) {
                    if (count[i] > 0) {
                        count[i]--

                        tasks.add(sequenceIdTasks[i])
                        tasks.add(cameraCaptureTasks[i])
                    }
                }
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        for (task in tasks) {
            if (cancellationToken.isDone) break
            task.execute(cancellationToken)
        }
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
