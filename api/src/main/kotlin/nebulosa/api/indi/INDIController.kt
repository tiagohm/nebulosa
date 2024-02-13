package nebulosa.api.indi

import jakarta.validation.Valid
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("indi")
class INDIController(
    private val indiService: INDIService,
    private val indiEventHandler: INDIEventHandler,
) {

    @GetMapping("{device}/properties")
    fun properties(@DeviceOrEntityParam device: Device): Collection<PropertyVector<*, *>> {
        return indiService.properties(device)
    }

    @PutMapping("{device}/send")
    fun sendProperty(
        @DeviceOrEntityParam device: Device,
        @RequestBody @Valid body: INDISendProperty,
    ) {
        return indiService.sendProperty(device, body)
    }

    @GetMapping("{device}/log")
    fun log(@DeviceOrEntityParam device: Device): List<String> {
        return synchronized(device.messages) { device.messages }
    }

    @GetMapping("log")
    fun log(): List<String> {
        return indiService.messages()
    }

    @Synchronized
    @PutMapping("listener/{device}/start")
    fun startListening(device: Device) {
        indiEventHandler.canSendEvents.add(device)
    }

    @Synchronized
    @PutMapping("listener/{device}/stop")
    fun stopListening(device: Device) {
        indiEventHandler.canSendEvents.remove(device)
    }
}
