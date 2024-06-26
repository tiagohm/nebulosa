package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.guide.GuideOutput
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
@Subscriber
class DARVExecutor(
    private val messageService: MessageService,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
) : Consumer<MessageEvent>, CameraEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<DARVJob>(1)

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Synchronized
    fun execute(camera: Camera, guideOutput: GuideOutput, request: DARVStartRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(guideOutput.connected) { "${guideOutput.name} Guide Output is not connected" }
        check(jobs.none { it.task.camera === camera }) { "${camera.name} DARV Job is already in progress" }
        check(jobs.none { it.task.guideOutput === guideOutput }) { "${camera.name} DARV Job is already in progress" }

        val task = DARVTask(camera, guideOutput, request, threadPoolTaskExecutor)
        task.subscribe(this)

        with(DARVJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }

    fun status(camera: Camera): DARVEvent? {
        return jobs.find { it.task.camera === camera }?.task?.get() as? DARVEvent
    }
}
