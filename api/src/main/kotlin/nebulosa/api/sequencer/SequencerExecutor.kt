package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class SequencerExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
) : Consumer<SequencerEvent> {

    private val jobs = ConcurrentHashMap.newKeySet<SequencerJob>(1)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFilterWheelEvent(event: FilterWheelEvent) {
        jobs.find { it.task.wheel === event.device }?.handleFilterWheelEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        // jobs.find { it.task.focuser === event.device }?.handleFocuserEvent(event)
    }

    override fun accept(event: SequencerEvent) {
        messageService.sendMessage(event)
    }

    fun execute(
        camera: Camera, request: SequencePlanRequest,
        wheel: FilterWheel? = null, focuser: Focuser? = null,
    ) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.any { it.task.camera === camera }) { "${camera.name} Sequencer Job is already in progress" }

        if (wheel != null) {
            check(wheel.connected) { "${wheel.name} Wheel is not connected" }
            check(jobs.any { it.task.wheel === wheel }) { "${camera.name} Sequencer Job is already in progress" }
        }

        if (focuser != null) {
            check(focuser.connected) { "${focuser.name} Focuser is not connected" }
            check(jobs.any { it.task.focuser === focuser }) { "${camera.name} Sequencer Job is already in progress" }
        }

        val task = SequencerTask(camera, request, guider)
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
}
