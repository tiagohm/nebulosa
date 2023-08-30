package nebulosa.api.guiding

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.guide.GuideOutputEvent
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GuideOutputEventHandler(
    private val eventBus: EventBus,
    private val messageService: MessageService,
    private val service: GuidingService,
) : DeviceEventHandler {

    private val throttler = PublishSubject.create<GuideOutputEvent<*>>()

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)

        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device) }
    }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is GuideOutputEvent<*> &&
            event.device.canPulseGuide
        ) {
            when (event) {
                is PropertyChangedEvent -> {
                    throttler.onNext(event)
                }
                is GuideOutputAttached -> {
                    service.add(event.device)
                    messageService.sendMessage(GUIDE_OUTPUT_ATTACHED, event.device)
                }
                is GuideOutputDetached -> {
                    service.remove(event.device)
                    messageService.sendMessage(GUIDE_OUTPUT_DETACHED, event.device)
                }
            }
        }
    }

    fun sendUpdate(device: GuideOutput) {
        messageService.sendMessage(GUIDE_OUTPUT_UPDATED, device)
    }

    companion object {

        const val GUIDE_OUTPUT_UPDATED = "GUIDE_OUTPUT_UPDATED"
        const val GUIDE_OUTPUT_ATTACHED = "GUIDE_OUTPUT_ATTACHED"
        const val GUIDE_OUTPUT_DETACHED = "GUIDE_OUTPUT_DETACHED"
    }
}
