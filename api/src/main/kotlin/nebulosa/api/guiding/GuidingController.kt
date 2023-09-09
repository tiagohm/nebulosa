package nebulosa.api.guiding

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.api.data.responses.GuidingChartResponse
import nebulosa.api.data.responses.GuidingStarResponse
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GuidingController(
    private val connectionService: ConnectionService,
    private val guidingService: GuidingService,
) {

    @GetMapping("attachedGuideOutputs")
    fun guideOutputs(): List<GuideOutput> {
        return connectionService.guideOutputs()
    }

    @GetMapping("guideOutput")
    fun guideOutput(@RequestParam @Valid @NotBlank name: String): GuideOutput {
        return requireNotNull(connectionService.guideOutput(name))
    }

    @PostMapping("guideOutputConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val guideOutput = requireNotNull(connectionService.guideOutput(name))
        guidingService.connect(guideOutput)
    }

    @PostMapping("guideOutputDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val guideOutput = requireNotNull(connectionService.guideOutput(name))
        guidingService.disconnect(guideOutput)
    }

    @PostMapping("guideLoopingStart")
    fun startLooping(
        @RequestParam("camera") @Valid @NotBlank cameraName: String,
        @RequestParam("mount") @Valid @NotBlank mountName: String,
        @RequestParam("guideOutput") @Valid @NotBlank guideOutputName: String,
        @RequestBody @Valid body: GuideStartLoopingRequest,
    ) {
        val camera = requireNotNull(connectionService.camera(cameraName))
        val mount = requireNotNull(connectionService.mount(mountName))
        val guideOutput = requireNotNull(connectionService.guideOutput(guideOutputName))
        guidingService.startLooping(camera, mount, guideOutput, body)
    }

    @PostMapping("guidingStart")
    fun startGuiding(
        @RequestParam(required = false, defaultValue = "false") forceCalibration: Boolean,
    ) {
        guidingService.startGuiding(forceCalibration)
    }

    @PostMapping("guidingStop")
    fun stopGuiding() {
        guidingService.stop()
    }

    @GetMapping("guidingChart")
    fun guidingChart(): GuidingChartResponse {
        return guidingService.guidingChart()
    }

    @GetMapping("guidingStar")
    fun guidingStar(): GuidingStarResponse? {
        return guidingService.guidingStar()
    }

    @PostMapping("selectGuideStar")
    fun selectGuideStar(
        @RequestParam @Valid @PositiveOrZero x: Double,
        @RequestParam @Valid @PositiveOrZero y: Double,
    ) {
        guidingService.selectGuideStar(x, y)
    }

    @PostMapping("deselectGuideStar")
    fun deselectGuideStar() {
        guidingService.deselectGuideStar()
    }
}
