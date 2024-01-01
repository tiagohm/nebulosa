package nebulosa.api.wizard.flat

import jakarta.validation.Valid
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.indi.device.camera.Camera
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("flat-wizard")
class FlatWizardController(
    private val flatWizardService: FlatWizardService,
) {

    @PutMapping("{camera}/start")
    fun startCapture(@DeviceOrEntityParam camera: Camera, @RequestBody @Valid body: FlatWizardRequest) {
        flatWizardService.startCapture(camera, body)
    }

    @PutMapping("{camera}/stop")
    fun stopCapture(@DeviceOrEntityParam camera: Camera) {
        flatWizardService.stopCapture(camera)
    }
}
