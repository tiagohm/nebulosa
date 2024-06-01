package nebulosa.api.autofocus

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.focuser.Focuser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("auto-focus")
class AutoFocusController(private val autoFocusService: AutoFocusService) {

    @PutMapping("{camera}/{focuser}/start")
    fun start(
        camera: Camera, focuser: Focuser,
        @RequestBody body: AutoFocusRequest,
    ) = autoFocusService.start(camera, focuser, body)

    @PutMapping("{camera}/stop")
    fun stop(camera: Camera) = autoFocusService.stop(camera)

    @GetMapping("{camera}/status")
    fun status(camera: Camera) = autoFocusService.status(camera)
}
