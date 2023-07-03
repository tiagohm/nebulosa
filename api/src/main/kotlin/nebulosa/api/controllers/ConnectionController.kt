package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.requests.ConnectionRequest
import nebulosa.api.services.ConnectionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("connection")
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PutMapping
    fun connect(@RequestBody @Valid body: ConnectionRequest) {
        connectionService.connect(body)
    }

    @DeleteMapping
    fun disconnect() {
        connectionService.disconnect()
    }

    @GetMapping
    fun isConnected(): Boolean {
        return connectionService.isConnected()
    }
}
