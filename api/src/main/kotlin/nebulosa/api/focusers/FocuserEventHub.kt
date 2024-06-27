package nebulosa.api.focusers

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class FocuserEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<Focuser, FocuserEvent>("FOCUSER"), FocuserEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is FocuserAttached -> onAttached(event.device)
            is FocuserDetached -> onDetached(event.device)
        }
    }

    override fun sendMessage(eventName: String, device: Focuser) {
        messageService.sendMessage(FocuserMessageEvent(eventName, device))
    }
}
