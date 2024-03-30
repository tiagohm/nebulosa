package nebulosa.api.sequencer

import jakarta.validation.Valid
import nebulosa.api.beans.converters.device.DeviceOrEntityParam
import nebulosa.indi.device.camera.Camera
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sequencer")
class SequencerController(
    private val sequencerService: SequencerService,
) {

    @PutMapping("{camera}/start")
    fun startSequencer(
        @DeviceOrEntityParam camera: Camera,
        @RequestBody @Valid body: SequencePlanRequest,
    ) = sequencerService.start(camera, body)

    @PutMapping("{camera}/stop")
    fun stopSequencer(@DeviceOrEntityParam camera: Camera) {
        sequencerService.stop(camera)
    }
}
