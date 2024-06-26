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

    @GetMapping("{device}")
    fun device(device: Device): Device {
        return device
    }

    @PutMapping("{device}/connect")
    fun connect(device: Device) {
        indiService.connect(device)
    }

    @PutMapping("{device}/disconnect")
    fun disconnect(device: Device) {
        indiService.disconnect(device)
    }

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
    @PutMapping("{device}/listen")
    fun listen(device: Device) {
        indiService.registerDeviceToSendMessage(device)
    }

    @Synchronized
    @PutMapping("{device}/unlisten")
    fun unlisten(device: Device) {
        indiService.unregisterDeviceToSendMessage(device)
    }
}
