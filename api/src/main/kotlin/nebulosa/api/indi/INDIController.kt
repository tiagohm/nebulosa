package nebulosa.api.indi

import jakarta.validation.Valid
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("indi")
class INDIController(
    private val indiService: INDIService,
) {

    @GetMapping("{device}/properties")
    fun properties(device: Device): Collection<PropertyVector<*, *>> {
        return indiService.properties(device)
    }

    @PutMapping("{device}/send")
    fun sendProperty(
        device: Device,
        @RequestBody @Valid body: INDISendProperty,
    ) {
        return indiService.sendProperty(device, body)
    }

    @GetMapping("{device}/log")
    fun log(device: Device): List<String> {
        return synchronized(device.messages) { device.messages }
    }

    @GetMapping("log")
    fun log(): List<String> {
        return indiService.messages()
    }

    @Synchronized
    @PutMapping("listener/{device}/start")
    fun startListening(device: Device) {
        indiService.registerDeviceToSendMessage(device)
    }

    @Synchronized
    @PutMapping("listener/{device}/stop")
    fun stopListening(device: Device) {
        indiService.unregisterDeviceToSendMessage(device)
    }
}
