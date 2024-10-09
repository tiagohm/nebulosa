package nebulosa.api.guiding

import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.guider.GuideOutputAttached
import nebulosa.indi.device.guider.GuideOutputDetached
import nebulosa.indi.device.guider.GuideOutputEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GuideOutputEventHub(
    private val messageService: MessageService,
    eventBus: EventBus,
) : DeviceEventHub<GuideOutput, GuideOutputEvent>(DeviceType.GUIDE_OUTPUT), GuideOutputEventAware {

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleGuideOutputEvent(event: GuideOutputEvent) {
        if (event.device.type == DeviceType.GUIDE_OUTPUT) {
            when (event) {
                is PropertyChangedEvent -> if (event.device.canPulseGuide) onNext(event)
                is GuideOutputAttached -> onAttached(event.device)
                is GuideOutputDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: GuideOutput) {
        messageService.sendMessage(GuideOutputMessageEvent(eventName, device))
    }
}
