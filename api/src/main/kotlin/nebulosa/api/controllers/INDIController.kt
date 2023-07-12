package nebulosa.api.controllers

import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.api.services.INDIService
import nebulosa.api.services.WebSocketService
import nebulosa.indi.device.DevicePropertyChanged
import nebulosa.indi.device.DevicePropertyDeleted
import nebulosa.indi.device.DevicePropertyEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
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

    @GetMapping("indiProperties")
    fun properties(@RequestParam @Valid @NotBlank name: String): List<INDIPropertyResponse> {
        return indiService.properties(name)
    }

    @PostMapping("sendIndiProperty")
    fun sendProperty(
        @RequestParam @Valid @NotBlank name: String,
        @RequestBody @Valid body: INDISendPropertyRequest,
    ) {
        return indiService.sendProperty(name, body)
    }
}
