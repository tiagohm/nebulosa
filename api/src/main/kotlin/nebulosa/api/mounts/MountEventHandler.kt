package nebulosa.api.mounts

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.mount.MountEvent
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MountEventHandler(
    private val eventBus: EventBus,
    private val messageService: MessageService,
    private val service: MountService,
) : DeviceEventHandler {

    private val throttler = PublishSubject.create<MountEvent>()

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)

        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is MountEvent) {
            when (event) {
                is PropertyChangedEvent -> {
                    throttler.onNext(event)
                }
                is MountAttached -> {
                    service.add(event.device)
                    messageService.sendMessage(MOUNT_ATTACHED, event.device)
                }
                is MountDetached -> {
                    service.remove(event.device)
                    messageService.sendMessage(MOUNT_DETACHED, event.device)
                }
            }
        }
    }

    fun sendUpdate(device: Mount) {
        messageService.sendMessage(MOUNT_UPDATED, device)
    }

    companion object {

        const val MOUNT_UPDATED = "MOUNT_UPDATED"
        const val MOUNT_ATTACHED = "MOUNT_ATTACHED"
        const val MOUNT_DETACHED = "MOUNT_DETACHED"
    }
}
