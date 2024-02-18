package nebulosa.api.connection

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("connection")
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PutMapping
    fun connect(
        @RequestParam @Valid @NotBlank host: String,
        @RequestParam @Valid @Range(min = 1, max = 65535) port: Int,
        @RequestParam(required = false, defaultValue = "INDI") type: ConnectionType,
    ) = connectionService.connect(host, port, type)

    @DeleteMapping("{id}")
    fun disconnect(@PathVariable id: String) {
        connectionService.disconnect(id)
    }

    @GetMapping("{id}")
    fun connectionStatus(@PathVariable id: String): Boolean {
        return connectionService.connectionStatus(id)
    }
}
