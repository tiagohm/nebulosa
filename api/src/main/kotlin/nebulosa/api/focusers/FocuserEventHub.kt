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
) : DeviceEventHub<Focuser, FocuserEvent>(), FocuserEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is FocuserAttached -> sendMessage(FOCUSER_ATTACHED, event.device)
            is FocuserDetached -> sendMessage(FOCUSER_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Focuser) {
        messageService.sendMessage(FocuserMessageEvent(eventName, device))
    }

    override fun sendUpdate(device: Focuser) {
        sendMessage(FOCUSER_UPDATED, device)
    }

    companion object {

        const val FOCUSER_UPDATED = "FOCUSER.UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER.ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER.DETACHED"
    }
}
