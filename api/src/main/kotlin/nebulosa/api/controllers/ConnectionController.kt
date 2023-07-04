package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.services.ConnectionService
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("connection")
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PostMapping
    fun connect(
        @RequestParam @Valid @NotBlank host: String,
        @RequestParam @Valid @Range(min = 1, max = 65535) port: Int,
    ) {
        connectionService.connect(host, port)
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
