package nebulosa.api.mounts

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.mount.MountEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class MountEventHandler(
    private val messageService: MessageService,
) {

    private val throttler = PublishSubject.create<MountEvent>()

    @PostConstruct
    private fun initialize() {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        when (event) {
            is PropertyChangedEvent -> {
                throttler.onNext(event)
            }
            is MountAttached -> {
                messageService.sendMessage(MountMessageEvent(MOUNT_ATTACHED, event.device))
            }
            is MountDetached -> {
                messageService.sendMessage(MountMessageEvent(MOUNT_DETACHED, event.device))
            }
        }
    }

    fun sendUpdate(device: Mount) {
        messageService.sendMessage(MountMessageEvent(MOUNT_UPDATED, device))
    }

    companion object {

        const val MOUNT_UPDATED = "MOUNT_UPDATED"
        const val MOUNT_ATTACHED = "MOUNT_ATTACHED"
        const val MOUNT_DETACHED = "MOUNT_DETACHED"
    }
}
