package nebulosa.api.connection

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("connection")
class ConnectionController {

    @Autowired
    private lateinit var connectionService: ConnectionService

    @PostMapping("connect")
    fun connect(@RequestBody @Valid connect: Connect) {
        connectionService.connect(connect)
    }

    @PostMapping("disconnect")
    fun disconnect() {
        connectionService.disconnect()
    }

    @GetMapping("status")
    fun status(): ConnectionStatus {
        return connectionService.status()
    }
}
