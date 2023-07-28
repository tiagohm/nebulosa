package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.responses.FilterWheelResponse
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service

@Service
class FilterWheelService(
    private val equipmentService: EquipmentService,
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
            is FilterWheelAttached -> webSocketService.sendFilterWheelAttached(event.device)
            is FilterWheelDetached -> webSocketService.sendFilterWheelDetached(event.device)
        }
    }

    fun attachedFilterWheels(): List<FilterWheelResponse> {
        return equipmentService.filterWheels().map(::FilterWheelResponse)
    }

    operator fun get(name: String): FilterWheelResponse {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        return FilterWheelResponse(filterWheel)
    }

    fun connect(name: String) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheel.connect()
    }

    fun disconnect(name: String) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheel.disconnect()
    }

    fun moveTo(name: String, steps: Int) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheel.moveTo(steps)
    }

    fun syncNames(name: String, filterNames: List<String>) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheel.syncNames(filterNames)
    }
}
