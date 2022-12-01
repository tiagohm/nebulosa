package nebulosa.api.cameras

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("cameras")
class CameraController {

    @Autowired
    private lateinit var cameraService: CameraService

    @GetMapping
    fun list(): List<CameraRes> {
        return cameraService.cameras()
    }

    @GetMapping("{name}")
    fun get(@PathVariable name: String): CameraRes {
        return cameraService.camera(name)
    }

    @PostMapping("{name}/connect")
    fun connect(@PathVariable name: String) {
        cameraService.connect(name)
    }

    @PostMapping("{name}/disconnect")
    fun disconnect(@PathVariable name: String) {
        cameraService.disconnect(name)
    }

    @PostMapping("{name}/startcapture")
    fun startCapture(@PathVariable name: String, @RequestBody @Valid startCaptureReq: StartCaptureReq) {
        cameraService.startCapture(name, startCaptureReq)
    }

    @PostMapping("{name}/stopcapture")
    fun stopCapture(@PathVariable name: String) {
        cameraService.stopCapture(name)
    }
}
