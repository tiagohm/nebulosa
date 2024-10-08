package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.guider.GuideOutput
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
class DARVExecutor(
    private val messageService: MessageService,
    private val executorService: ExecutorService,
) : Consumer<MessageEvent>, CameraEventAware, Executor by executorService {

    private val jobs = ConcurrentHashMap.newKeySet<DARVJob>(1)

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    @Synchronized
    fun execute(camera: Camera, guideOutput: GuideOutput, request: DARVStartRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(guideOutput.connected) { "${guideOutput.name} Guide Output is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} DARV Job is already in progress" }
        check(jobs.none { it.guideOutput === guideOutput }) { "${camera.name} DARV Job is already in progress" }

        with(DARVJob(this, camera, guideOutput, request)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this) }
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.camera === camera }?.stop()
    }

    fun status(camera: Camera): DARVEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
