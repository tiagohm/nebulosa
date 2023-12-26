package nebulosa.api.sequencer

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sequencer")
class SequencerController(
    private val sequencerService: SequencerService,
) {

    @PutMapping("start")
    fun startSequencer(@RequestBody @Valid body: SequencePlanRequest) {
        sequencerService.startSequencer(body)
    }

    @PutMapping("stop")
    fun stopSequencer() {
        sequencerService.stopSequencer()
    }
}
