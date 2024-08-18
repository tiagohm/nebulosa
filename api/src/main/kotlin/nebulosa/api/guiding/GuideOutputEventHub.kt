package nebulosa.api.guiding

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.guider.GuideOutputAttached
import nebulosa.indi.device.guider.GuideOutputDetached
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class GuideOutputEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<GuideOutput, DeviceEvent<GuideOutput>>("GUIDE_OUTPUT"), GuideOutputEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleGuideOutputEvent(event: DeviceEvent<GuideOutput>) {
        val device = event.device ?: return

        if (event is PropertyChangedEvent) {
            if (device.canPulseGuide) {
                onNext(event)
            }
        } else {
            when (event) {
                is GuideOutputAttached -> onAttached(event.device)
                is GuideOutputDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: GuideOutput) {
        messageService.sendMessage(GuideOutputMessageEvent(eventName, device))
    }
}
