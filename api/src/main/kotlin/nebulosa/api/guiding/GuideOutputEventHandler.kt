package nebulosa.api.guiding

import io.reactivex.rxjava3.subjects.PublishSubject
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
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class GuideOutputEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<DeviceEvent<GuideOutput>>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onGuideOutputEvent(event: DeviceEvent<GuideOutput>) {
        if (event.device!!.canPulseGuide && event is PropertyChangedEvent) {
            throttler.onNext(event)
        }

        when (event) {
            is GuideOutputAttached -> sendMessage(GUIDE_OUTPUT_ATTACHED, event.device)
            is GuideOutputDetached -> sendMessage(GUIDE_OUTPUT_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: GuideOutput) {
        messageService.sendMessage(GuideOutputMessageEvent(eventName, device))
    }

    fun sendUpdate(device: GuideOutput) {
        sendMessage(GUIDE_OUTPUT_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val GUIDE_OUTPUT_UPDATED = "GUIDE_OUTPUT.UPDATED"
        const val GUIDE_OUTPUT_ATTACHED = "GUIDE_OUTPUT.ATTACHED"
        const val GUIDE_OUTPUT_DETACHED = "GUIDE_OUTPUT.DETACHED"
    }
}
