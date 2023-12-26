package nebulosa.api.mounts

import io.reactivex.rxjava3.subjects.PublishSubject
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
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class MountEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<MountEvent>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        when (event) {
            is PropertyChangedEvent -> throttler.onNext(event)
            is MountAttached -> sendMessage(MOUNT_ATTACHED, event.device)
            is MountDetached -> sendMessage(MOUNT_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Mount) {
        messageService.sendMessage(MountMessageEvent(eventName, device))
    }

    fun sendUpdate(device: Mount) {
        sendMessage(MOUNT_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val MOUNT_UPDATED = "MOUNT_UPDATED"
        const val MOUNT_ATTACHED = "MOUNT_ATTACHED"
        const val MOUNT_DETACHED = "MOUNT_DETACHED"
    }
}
