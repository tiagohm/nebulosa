package nebulosa.api.rotators

import jakarta.validation.Valid
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.rotator.Rotator
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("rotators")
class RotatorController(
    private val connectionService: ConnectionService,
    private val rotatorService: RotatorService,
) {

    @GetMapping
    fun rotators(): List<Rotator> {
        return connectionService.rotators().sorted()
    }

    @GetMapping("{rotator}")
    fun rotator(rotator: Rotator): Rotator {
        return rotator
    }

    @PutMapping("{rotator}/connect")
    fun connect(rotator: Rotator) {
        rotatorService.connect(rotator)
    }

    @PutMapping("{rotator}/disconnect")
    fun disconnect(rotator: Rotator) {
        rotatorService.disconnect(rotator)
    }

    @PutMapping("{rotator}/move")
    fun moveIn(
        rotator: Rotator,
        @RequestParam @Valid @Range(min = 0, max = 360) angle: Double,
    ) {
        rotatorService.move(rotator, angle)
    }

    @PutMapping("{rotator}/abort")
    fun abort(rotator: Rotator) {
        rotatorService.abort(rotator)
    }

    @PutMapping("{rotator}/sync")
    fun sync(
        rotator: Rotator,
        @RequestParam @Valid @Range(min = 0, max = 360) angle: Double,
    ) {
        rotatorService.sync(rotator, angle)
    }
}
