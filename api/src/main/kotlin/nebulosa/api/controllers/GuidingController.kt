package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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
        return equipmentService.mounts() + equipmentService.cameras()
    }

    @GetMapping("attachedGuideOutput")
    fun guideOutput(@RequestParam @Valid @NotBlank name: String): GuideOutput {
        return requireNotNull(equipmentService.mount(name) ?: equipmentService.camera(name))
    }

    @PostMapping("startLooping")
    fun startLooping(
        @RequestParam @Valid @NotBlank camera: String,
        @RequestParam @Valid @NotBlank mount: String,
        @RequestParam @Valid @NotBlank guideOutput: String,
    ) {
        guidingService.startLooping(
            equipmentService.camera(camera)!!, equipmentService.mount(mount)!!,
            equipmentService[guideOutput] as GuideOutput,
        )
    }

    @PostMapping("stopLooping")
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
}
