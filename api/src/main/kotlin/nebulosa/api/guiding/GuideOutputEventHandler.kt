package nebulosa.api.guiding

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class GuideOutputEventHandler(
    private val messageService: MessageService,
) {

    private val throttler = PublishSubject.create<DeviceEvent<GuideOutput>>()

    @PostConstruct
    private fun initialize() {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onGuideOutputEvent(event: DeviceEvent<GuideOutput>) {
        val device = event.device ?: return

        if (device.canPulseGuide) {
            when (event) {
                is PropertyChangedEvent -> {
                    throttler.onNext(event)
                }
                is GuideOutputAttached -> {
                    messageService.sendMessage(GuideOutputMessageEvent(GUIDE_OUTPUT_ATTACHED, event.device))
                }
                is GuideOutputDetached -> {
                    messageService.sendMessage(GuideOutputMessageEvent(GUIDE_OUTPUT_DETACHED, event.device))
                }
            }
        }
    }

    fun sendUpdate(device: GuideOutput) {
        messageService.sendMessage(GuideOutputMessageEvent(GUIDE_OUTPUT_UPDATED, device))
    }

    companion object {

        const val GUIDE_OUTPUT_UPDATED = "GUIDE_OUTPUT_UPDATED"
        const val GUIDE_OUTPUT_ATTACHED = "GUIDE_OUTPUT_ATTACHED"
        const val GUIDE_OUTPUT_DETACHED = "GUIDE_OUTPUT_DETACHED"
    }
}
