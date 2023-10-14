package nebulosa.api.guiding

import nebulosa.api.beans.annotations.EntityBy
import nebulosa.api.connection.ConnectionService
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("guide-outputs")
class GuideOutputController(
    private val connectionService: ConnectionService,
    private val guideOutputService: GuideOutputService,
) {

    @GetMapping
    fun guideOutputs(): List<GuideOutput> {
        return connectionService.guideOutputs()
    }

    @GetMapping("{guideOutput}")
    fun guideOutput(@EntityBy guideOutput: GuideOutput): GuideOutput {
        return guideOutput
    }

    @PutMapping("{guideOutput}/connect")
    fun connect(@EntityBy guideOutput: GuideOutput) {
        guideOutputService.connect(guideOutput)
    }

    @PutMapping("{guideOutput}/disconnect")
    fun disconnect(@EntityBy guideOutput: GuideOutput) {
        guideOutputService.disconnect(guideOutput)
    }

    @PutMapping("{guideOutput}/pulse")
    fun pulse(
        @EntityBy guideOutput: GuideOutput,
        @RequestParam direction: GuideDirection, @RequestParam duration: Int,
    ) {
        guideOutputService.pulse(guideOutput, direction, duration)
    }
}
