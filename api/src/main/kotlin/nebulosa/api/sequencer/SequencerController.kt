package nebulosa.api.sequencer

import jakarta.validation.Valid
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("sequencer")
class SequencerController(
    private val sequencerService: SequencerService,
) {

    @PutMapping("{camera}/start")
    fun start(
        camera: Camera,
        mount: Mount?, wheel: FilterWheel?, focuser: Focuser?,
        @RequestBody @Valid body: SequencePlanRequest,
    ) = sequencerService.start(camera, body, mount, wheel, focuser)

    @PutMapping("{camera}/stop")
    fun stop(camera: Camera) {
        sequencerService.stop(camera)
    }

    @GetMapping("{camera}/status")
    fun status(camera: Camera): SequencerEvent? {
        return sequencerService.status(camera)
    }
}
