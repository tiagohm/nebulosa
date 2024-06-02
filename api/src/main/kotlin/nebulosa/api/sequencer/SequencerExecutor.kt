package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.api.wheels.WheelEventAware
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.mount.Mount
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class SequencerExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
) : Consumer<MessageEvent>, CameraEventAware, WheelEventAware, FocuserEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<SequencerJob>(1)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        jobs.find { it.task.wheel === event.device }?.handleFilterWheelEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        // jobs.find { it.task.focuser === event.device }?.handleFocuserEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    fun execute(
        camera: Camera, request: SequencePlanRequest,
        mount: Mount? = null, wheel: FilterWheel? = null, focuser: Focuser? = null,
    ) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.none { it.task.camera === camera }) { "${camera.name} Sequencer Job is already in progress" }

        if (wheel != null && wheel.connected) {
            check(jobs.none { it.task.wheel === wheel }) { "${camera.name} Sequencer Job is already in progress" }
        }

        if (focuser != null && focuser.connected) {
            check(jobs.none { it.task.focuser === focuser }) { "${camera.name} Sequencer Job is already in progress" }
        }

        val task = SequencerTask(camera, request, guider, mount, wheel, focuser, threadPoolTaskExecutor)
        task.subscribe(this)

        with(SequencerJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }

    fun status(camera: Camera): SequencerEvent? {
        return jobs.find { it.task.camera === camera }?.task?.get() as? SequencerEvent
    }
}
