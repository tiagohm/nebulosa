package nebulosa.api.lightboxes

import jakarta.validation.Valid
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.lightbox.LightBox
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("light-boxes")
class LightBoxController(
    private val connectionService: ConnectionService,
    private val lightBoxService: LightBoxService,
) {

    @GetMapping
    fun lightBoxes(): List<LightBox> {
        return connectionService.lightBoxes().sorted()
    }

    @GetMapping("{lightBox}")
    fun lightBox(lightBox: LightBox): LightBox {
        return lightBox
    }

    @PutMapping("{lightBox}/connect")
    fun connect(lightBox: LightBox) {
        lightBoxService.connect(lightBox)
    }

    @PutMapping("{lightBox}/disconnect")
    fun disconnect(lightBox: LightBox) {
        lightBoxService.disconnect(lightBox)
    }

    @PutMapping("{lightBox}/enable")
    fun enable(lightBox: LightBox) {
        lightBoxService.enable(lightBox)
    }

    @PutMapping("{lightBox}/disable")
    fun disable(lightBox: LightBox) {
        lightBoxService.disable(lightBox)
    }

    @PutMapping("{lightBox}/brightness")
    fun brightness(lightBox: LightBox, @RequestParam @Valid @PositiveOrZero intensity: Double) {
        lightBoxService.brightness(lightBox, intensity)
    }
}
