package nebulosa.api.focusers

import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FocuserEventHub(
    private val messageService: MessageService,
    eventBus: EventBus,
) : DeviceEventHub<Focuser, FocuserEvent>(DeviceType.FOCUSER), FocuserEventAware {

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device.type == DeviceType.FOCUSER) {
            when (event) {
                is PropertyChangedEvent -> onNext(event)
                is FocuserAttached -> onAttached(event.device)
                is FocuserDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: Focuser) {
        messageService.sendMessage(FocuserMessageEvent(eventName, device))
    }
}
