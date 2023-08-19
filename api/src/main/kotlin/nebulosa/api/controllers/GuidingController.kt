package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.data.responses.GuidingChartResponse
import nebulosa.api.data.responses.GuidingStarResponse
import nebulosa.api.services.EquipmentService
import nebulosa.api.services.GuidingService
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GuidingController(
    private val guidingService: GuidingService,
    private val equipmentService: EquipmentService,
) {

    @GetMapping("attachedGuideOutputs")
    fun guideOutputs(): List<GuideOutput> {
        return equipmentService.guideOutputs()
    }

    @GetMapping("guideOutput")
    fun guideOutput(@RequestParam @Valid @NotBlank name: String): GuideOutput {
        return requireNotNull(equipmentService.guideOutput(name))
    }

    @PostMapping("guideOutputConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val guideOutput = requireNotNull(equipmentService.guideOutput(name))
        guidingService.connect(guideOutput)
    }

    @PostMapping("guideOutputDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val guideOutput = requireNotNull(equipmentService.guideOutput(name))
        guidingService.disconnect(guideOutput)
    }

    @PostMapping("startGuideLooping")
    fun startLooping(
        @RequestParam @Valid @NotBlank camera: String,
        @RequestParam @Valid @NotBlank mount: String,
        @RequestParam @Valid @NotBlank guideOutput: String,
    ) {
        guidingService.startLooping(
            equipmentService.camera(camera)!!, equipmentService.mount(mount)!!,
            equipmentService.guideOutput(guideOutput)!!,
        )
    }

    @PostMapping("stopGuideLooping")
    fun stopLooping() {
        guidingService.stopLooping()
    }

    @PostMapping("startGuiding")
    fun startGuiding(
        @RequestParam(required = false, defaultValue = "false") forceCalibration: Boolean,
    ) {
        guidingService.startGuiding(forceCalibration)
    }

    @PostMapping("stopGuiding")
    fun stopGuiding() {
        guidingService.stopGuiding()
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
