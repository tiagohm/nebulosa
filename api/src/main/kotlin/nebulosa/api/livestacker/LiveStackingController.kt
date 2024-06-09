package nebulosa.api.livestacker

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.indi.device.camera.Camera
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("live-stacking")
class LiveStackingController(private val liveStackingService: LiveStackingService) {

    @PutMapping("{camera}/start")
    fun start(camera: Camera, @RequestBody body: LiveStackingRequest) {
        liveStackingService.start(camera, body)
    }

    @PutMapping("{camera}/add")
    fun add(camera: Camera, @RequestParam @Valid @NotBlank path: Path): Path? {
        return liveStackingService.add(camera, path)
    }

    @PutMapping("{camera}/stop")
    fun stop(camera: Camera) {
        liveStackingService.stop(camera)
    }
}
