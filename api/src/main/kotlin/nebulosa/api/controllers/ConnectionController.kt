package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.controllers.dtos.ConnectionReq
import nebulosa.api.controllers.dtos.ConnectionStatusRes
import nebulosa.api.services.ConnectionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("connection")
class ConnectionController {

    @Autowired
    private lateinit var connectionService: ConnectionService

    @PostMapping("connect")
    fun connect(@RequestBody @Valid connectionReq: ConnectionReq) {
        return connectionService.connection(connectionReq)
    }

    @PostMapping("disconnect")
    fun disconnect() {
        return connectionService.disconnect()
    }

    @GetMapping("status")
    fun status(): ConnectionStatusRes {
        return connectionService.status()
    }
}
