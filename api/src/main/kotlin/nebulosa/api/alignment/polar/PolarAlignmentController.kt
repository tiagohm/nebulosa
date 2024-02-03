package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("polar-alignment")
class PolarAlignmentController(
    private val polarAlignmentService: PolarAlignmentService,
) {

    @PutMapping("darv/{camera}/{guideOutput}/start")
    fun darvStart(
        @DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam guideOutput: GuideOutput,
        @RequestBody body: DARVStartRequest,
    ) = polarAlignmentService.darvStart(camera, guideOutput, body)

    @PutMapping("darv/{camera}/{guideOutput}/stop")
    fun darvStop(@DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam guideOutput: GuideOutput) {
        polarAlignmentService.darvStop(camera, guideOutput)
    }

    @PutMapping("tppa/{camera}/{mount}/start")
    fun tppaStart(
        @DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam mount: Mount,
        @RequestBody body: TPPAStartRequest,
    ) = polarAlignmentService.tppaStart(camera, mount, body)

    @PutMapping("tppa/{camera}/{mount}/stop")
    fun tppaStop(@DeviceOrEntityParam camera: Camera, @DeviceOrEntityParam mount: Mount) {
        polarAlignmentService.tppaStop(camera, mount)
    }
}
