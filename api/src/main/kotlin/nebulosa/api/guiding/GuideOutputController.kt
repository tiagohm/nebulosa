package nebulosa.api.guiding

import nebulosa.api.connection.ConnectionService
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("guide-outputs")
class GuideOutputController(
    private val connectionService: ConnectionService,
    private val guideOutputService: GuideOutputService,
) {

    @GetMapping
    fun guideOutputs(): List<GuideOutput> {
        return connectionService.guideOutputs().sorted()
    }

    @GetMapping("{guideOutput}")
    fun guideOutput(guideOutput: GuideOutput): GuideOutput {
        return guideOutput
    }

    @PutMapping("{guideOutput}/connect")
    fun connect(guideOutput: GuideOutput) {
        guideOutputService.connect(guideOutput)
    }

    @PutMapping("{guideOutput}/disconnect")
    fun disconnect(guideOutput: GuideOutput) {
        guideOutputService.disconnect(guideOutput)
    }

    @PutMapping("{guideOutput}/pulse")
    fun pulse(
        guideOutput: GuideOutput,
        @RequestParam direction: GuideDirection,
        @RequestParam @DurationMin(nanos = 0L) @DurationMax(seconds = 60L) duration: Duration,
    ) {
        guideOutputService.pulse(guideOutput, direction, duration)
    }
}
