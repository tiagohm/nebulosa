package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVExecutor
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Service

@Service
class PolarAlignmentService(
    private val darvExecutor: DARVExecutor,
) {

    fun darvStart(camera: Camera, guideOutput: GuideOutput, darvStartRequest: DARVStartRequest) {
        check(camera.connected) { "camera not connected" }
        check(guideOutput.connected) { "guide output not connected" }
        darvExecutor.execute(darvStartRequest.copy(camera = camera, guideOutput = guideOutput))
    }

    fun darvStop(camera: Camera, guideOutput: GuideOutput) {
        darvExecutor.stop(camera, guideOutput)
    }
}
