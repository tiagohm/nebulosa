package nebulosa.api.controllers

import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.api.services.EquipmentService
import nebulosa.api.services.INDIService
import nebulosa.api.services.WebSocketService
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.DevicePropertyChanged
import nebulosa.indi.device.DevicePropertyDeleted
import nebulosa.indi.device.DevicePropertyEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
    private val equipmentService: EquipmentService,
    private val indiService: INDIService,
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDevicePropertyEvent(event: DevicePropertyEvent) {
        when (event) {
            is DevicePropertyChanged -> webSocketService.sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> webSocketService.sendINDIPropertyDeleted(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDeviceMessageReceived(event: DeviceMessageReceived) {
        if (event.device == null) {
            indiService.addFirst(event.message)
        }

        webSocketService.sendINDIMessageReceived(event)
    }

    @GetMapping("indiProperties")
    fun properties(@RequestParam @Valid @NotBlank name: String): List<INDIPropertyResponse> {
        val device = requireNotNull(equipmentService[name])
        return indiService.properties(device)
    }

    @PostMapping("sendIndiProperty")
    fun sendProperty(
        @RequestParam @Valid @NotBlank name: String,
        @RequestBody @Valid body: INDISendPropertyRequest,
    ) {
        val device = requireNotNull(equipmentService[name])
        return indiService.sendProperty(device, body)
    }

    @GetMapping("indiLog")
    fun indiLog(@RequestParam(required = false) name: String?): List<String> {
        if (name.isNullOrBlank()) return indiService
        val device = equipmentService[name] ?: return emptyList()
        return device.messages
    }

    @PostMapping("indiStartListening")
    fun indiStartListening(@RequestParam("eventName") @Valid @NotEmpty eventNames: List<String>) {
        return eventNames.forEach(webSocketService::registerEventName)
    }

    @PostMapping("indiStopListening")
    fun indiStopListening(@RequestParam("eventName") @Valid @NotEmpty eventNames: List<String>) {
        return eventNames.forEach(webSocketService::unregisterEventName)
    }
}
