package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service

@Service
class FilterWheelService(
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FilterWheelEvent) {
        when (event) {
            is PropertyChangedEvent -> webSocketService.sendFilterWheelUpdated(event.device!!)
            is FilterWheelAttached -> webSocketService.sendFilterWheelAttached(event)
            is FilterWheelDetached -> webSocketService.sendFilterWheelDetached(event)
        }
    }

    fun connect(filterWheel: FilterWheel) {
        filterWheel.connect()
    }

    fun disconnect(filterWheel: FilterWheel) {
        filterWheel.disconnect()
    }

    fun moveTo(filterWheel: FilterWheel, steps: Int) {
        filterWheel.moveTo(steps)
    }

    fun syncNames(filterWheel: FilterWheel, filterNames: List<String>) {
        filterWheel.syncNames(filterNames)
    }
}
