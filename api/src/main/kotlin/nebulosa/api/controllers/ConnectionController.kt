package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.dtos.ConnectionRequest
import nebulosa.api.services.ConnectionService
import org.springframework.web.bind.annotation.*

@RestController
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PostMapping("connect")
    fun connect(@RequestBody @Valid body: ConnectionRequest) {
        connectionService.connect(body)
    }

    @PostMapping("disconnect")
    fun disconnect() {
        connectionService.disconnect()
    }

    @GetMapping("isConnected")
    fun isConnected(): Boolean {
        return connectionService.isConnected()
    }
}
