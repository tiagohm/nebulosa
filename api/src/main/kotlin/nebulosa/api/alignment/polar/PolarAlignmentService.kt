package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVPolarAlignmentExecutor
import nebulosa.api.alignment.polar.darv.DARVStart
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Service

@Service
class PolarAlignmentService(
    private val darvPolarAlignmentExecutor: DARVPolarAlignmentExecutor,
) {

    fun darvStart(camera: Camera, guideOutput: GuideOutput, darvStart: DARVStart) {
        check(camera.connected) { "camera not connected" }
        check(guideOutput.connected) { "guide output not connected" }
        darvPolarAlignmentExecutor.execute(darvStart.copy(camera = camera, guideOutput = guideOutput))
    }

    fun darvStop(camera: Camera, guideOutput: GuideOutput) {
        darvPolarAlignmentExecutor.stop(camera, guideOutput)
    }
}
