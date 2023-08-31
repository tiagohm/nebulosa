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
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FocuserEventHandler(
    private val messageService: MessageService,
) : DeviceEventHandler {

    private val throttler = PublishSubject.create<FocuserEvent>()

    @PostConstruct
    private fun initialize() {
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
                    messageService.sendMessage(FOCUSER_ATTACHED, event.device)
                }
                is FocuserDetached -> {
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
