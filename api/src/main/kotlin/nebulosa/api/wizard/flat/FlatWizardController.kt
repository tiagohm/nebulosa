package nebulosa.api.wizard.flat

import jakarta.validation.Valid
import nebulosa.indi.device.camera.Camera
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("flat-wizard")
class FlatWizardController(
    private val flatWizardService: FlatWizardService,
) {

    @PutMapping("{camera}/start")
    fun start(camera: Camera, @RequestBody @Valid body: FlatWizardRequest) {
        flatWizardService.start(camera, body)
    }

    @PutMapping("{camera}/stop")
    fun stop(camera: Camera) {
        flatWizardService.stop(camera)
    }

    @GetMapping("{camera}/status")
    fun status(camera: Camera): FlatWizardEvent? {
        return flatWizardService.status(camera)
    }
}
