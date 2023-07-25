package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.services.ConnectionService
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PostMapping("connect")
    fun connect(
        @RequestParam @Valid @NotBlank host: String,
        @RequestParam @Valid @Range(min = 1, max = 65535) port: Int,
    ) {
        connectionService.connect(host, port)
    }

    @PostMapping("disconnect")
    fun disconnect() {
        connectionService.disconnect()
    }

    @GetMapping("connectionStatus")
    fun connectionStatus(): Boolean {
        return connectionService.connectionStatus()
    }
}
