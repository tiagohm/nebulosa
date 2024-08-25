package nebulosa.api.dustcap

import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.dustcap.DustCap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("dust-cap")
class DustCapController(
    private val connectionService: ConnectionService,
    private val dustCapService: DustCapService,
) {

    @GetMapping
    fun dustCaps(): List<DustCap> {
        return connectionService.dustCaps().sorted()
    }

    @GetMapping("{dustCap}")
    fun dustCap(dustCap: DustCap): DustCap {
        return dustCap
    }

    @PutMapping("{dustCap}/connect")
    fun connect(dustCap: DustCap) {
        dustCapService.connect(dustCap)
    }

    @PutMapping("{dustCap}/disconnect")
    fun disconnect(dustCap: DustCap) {
        dustCapService.disconnect(dustCap)
    }

    @PutMapping("{dustCap}/park")
    fun park(dustCap: DustCap) {
        dustCapService.park(dustCap)
    }

    @PutMapping("{dustCap}/unpark")
    fun unpark(dustCap: DustCap) {
        dustCapService.unpark(dustCap)
    }
}
