package nebulosa.api.controllers

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.api.services.EventEmitterService
import nebulosa.api.services.INDIService
import nebulosa.indi.device.DevicePropertyChanged
import nebulosa.indi.device.DevicePropertyDeleted
import nebulosa.indi.device.DevicePropertyEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class INDIController(
    private val indiService: INDIService,
    private val eventEmitterService: EventEmitterService,
    private val eventBus: EventBus,
) {

    private fun sendEvent(event: DevicePropertyEvent, type: String) {
        eventEmitterService.sendEvent("INDI.${event.device.name}", type, INDIPropertyResponse(event.property))
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDevicePropertyEvent(event: DevicePropertyEvent) {
        when (event) {
            is DevicePropertyChanged -> sendEvent(event, "DEVICE_PROPERTY_CHANGED")
            is DevicePropertyDeleted -> sendEvent(event, "DEVICE_PROPERTY_DELETED")
        }
    }

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @PreDestroy
    private fun destroy() {
        eventBus.unregister(this)
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

    @GetMapping("indiEvents")
    fun events(@RequestParam @Valid @NotBlank name: String): SseEmitter {
        return eventEmitterService.register("INDI.$name")
    }
}
