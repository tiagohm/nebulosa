package nebulosa.api.lightboxes

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.lightbox.LightBoxAttached
import nebulosa.indi.device.lightbox.LightBoxDetached
import nebulosa.indi.device.lightbox.LightBoxEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class LightBoxEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<LightBox, LightBoxEvent>(DeviceType.LIGHT_BOX), LightBoxEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleLightBoxEvent(event: LightBoxEvent) {
        if (event.device.type == DeviceType.ROTATOR) {
            when (event) {
                is PropertyChangedEvent -> onNext(event)
                is LightBoxAttached -> onAttached(event.device)
                is LightBoxDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: LightBox) {
        messageService.sendMessage(LightBoxMessageEvent(eventName, device))
    }
}
