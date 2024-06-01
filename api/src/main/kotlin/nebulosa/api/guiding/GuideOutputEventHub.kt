package nebulosa.api.guiding

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class GuideOutputEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<GuideOutput, DeviceEvent<GuideOutput>>(), GuideOutputEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleGuideOutputEvent(event: DeviceEvent<GuideOutput>) {
        val device = event.device ?: return

        if (event is PropertyChangedEvent) {
            if (device.canPulseGuide) {
                onNext(event)
            }
        } else {
            when (event) {
                is GuideOutputAttached -> sendMessage(GUIDE_OUTPUT_ATTACHED, event.device)
                is GuideOutputDetached -> sendMessage(GUIDE_OUTPUT_DETACHED, event.device)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: GuideOutput) {
        messageService.sendMessage(GuideOutputMessageEvent(eventName, device))
    }

    override fun sendUpdate(device: GuideOutput) {
        sendMessage(GUIDE_OUTPUT_UPDATED, device)
    }

    companion object {

        const val GUIDE_OUTPUT_UPDATED = "GUIDE_OUTPUT.UPDATED"
        const val GUIDE_OUTPUT_ATTACHED = "GUIDE_OUTPUT.ATTACHED"
        const val GUIDE_OUTPUT_DETACHED = "GUIDE_OUTPUT.DETACHED"
    }
}
