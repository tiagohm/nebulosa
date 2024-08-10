package nebulosa.api.sequencer

import jakarta.validation.Valid
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("sequencer")
class SequencerController(
    private val sequencerService: SequencerService,
) {

    @PutMapping("{camera}/start")
    fun start(
        camera: Camera,
        mount: Mount?, wheel: FilterWheel?, focuser: Focuser?, rotator: Rotator?,
        @RequestBody @Valid body: SequencerPlanRequest,
    ) = sequencerService.start(camera, body, mount, wheel, focuser, rotator)

    @PutMapping("{camera}/stop")
    fun stop(camera: Camera) {
        sequencerService.stop(camera)
    }

    @PutMapping("{camera}/pause")
    fun pause(camera: Camera) {
        sequencerService.pause(camera)
    }

    @PutMapping("{camera}/unpause")
    fun unpause(camera: Camera) {
        sequencerService.unpause(camera)
    }

    @GetMapping("{camera}/status")
    fun status(camera: Camera): SequencerEvent? {
        return sequencerService.status(camera)
    }
}
