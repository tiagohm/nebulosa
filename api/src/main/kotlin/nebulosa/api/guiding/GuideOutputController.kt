package nebulosa.api.guiding

import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
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
        return connectionService.guideOutputs()
    }

    @GetMapping("{guideOutput}")
    fun guideOutput(@DeviceOrEntityParam guideOutput: GuideOutput): GuideOutput {
        return guideOutput
    }

    @PutMapping("{guideOutput}/connect")
    fun connect(@DeviceOrEntityParam guideOutput: GuideOutput) {
        guideOutputService.connect(guideOutput)
    }

    @PutMapping("{guideOutput}/disconnect")
    fun disconnect(@DeviceOrEntityParam guideOutput: GuideOutput) {
        guideOutputService.disconnect(guideOutput)
    }

    @PutMapping("{guideOutput}/pulse")
    fun pulse(
        @DeviceOrEntityParam guideOutput: GuideOutput,
        @RequestParam direction: GuideDirection,
        @RequestParam @DurationMin(nanos = 0L) @DurationMax(seconds = 60L) duration: Duration,
    ) {
        guideOutputService.pulse(guideOutput, direction, duration)
    }
}
