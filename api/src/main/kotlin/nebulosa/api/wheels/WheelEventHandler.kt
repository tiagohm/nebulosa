package nebulosa.api.wheels

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class WheelEventHandler(
    private val eventBus: EventBus,
    private val messageService: MessageService,
    private val service: WheelService,
) : DeviceEventHandler {

    private val throttler = PublishSubject.create<FilterWheelEvent>()

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)

        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is FilterWheelEvent) {
            when (event) {
                is PropertyChangedEvent -> {
                    throttler.onNext(event)
                }
                is FilterWheelAttached -> {
                    service.add(event.device)
                    messageService.sendMessage(WHEEL_ATTACHED, event.device)
                }
                is FilterWheelDetached -> {
                    service.remove(event.device)
                    messageService.sendMessage(WHEEL_DETACHED, event.device)
                }
            }
        }
    }

    fun sendUpdate(device: FilterWheel) {
        messageService.sendMessage(WHEEL_UPDATED, device)
    }

    companion object {

        const val WHEEL_UPDATED = "WHEEL_UPDATED"
        const val WHEEL_ATTACHED = "WHEEL_ATTACHED"
        const val WHEEL_DETACHED = "WHEEL_DETACHED"
    }
}
