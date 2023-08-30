package nebulosa.api.focusers

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FocuserEventHandler(
    private val eventBus: EventBus,
    private val messageService: MessageService,
    private val service: FocuserService,
) : DeviceEventHandler {

    private val throttler = PublishSubject.create<FocuserEvent>()

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)

        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is FocuserEvent) {
            when (event) {
                is PropertyChangedEvent -> {
                    throttler.onNext(event)
                }
                is FocuserAttached -> {
                    service.add(event.device)
                    messageService.sendMessage(FOCUSER_ATTACHED, event.device)
                }
                is FocuserDetached -> {
                    service.remove(event.device)
                    messageService.sendMessage(FOCUSER_DETACHED, event.device)
                }
            }
        }
    }

    fun sendUpdate(device: Focuser) {
        messageService.sendMessage(FOCUSER_UPDATED, device)
    }

    companion object {

        const val FOCUSER_UPDATED = "FOCUSER_UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER_ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER_DETACHED"
    }
}
