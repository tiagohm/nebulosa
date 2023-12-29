package nebulosa.api.wizard.flat

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("flat-wizard")
class FlatWizardController(
    private val flatWizardService: FlatWizardService,
) {

    @PutMapping("start")
    fun startCapture(@RequestBody @Valid body: FlatWizardRequest) {
        flatWizardService.startCapture(body)
    }
}
