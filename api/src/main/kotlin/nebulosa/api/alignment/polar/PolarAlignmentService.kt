package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVExecutor
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAExecutor
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.stereotype.Service

@Service
class PolarAlignmentService(
    private val darvExecutor: DARVExecutor,
    private val tppaExecutor: TPPAExecutor,
) {

    fun darvStart(camera: Camera, guideOutput: GuideOutput, darvStartRequest: DARVStartRequest) {
        darvExecutor.execute(camera, guideOutput, darvStartRequest)
    }

    fun darvStop(camera: Camera) {
        darvExecutor.stop(camera)
    }

    fun tppaStart(camera: Camera, mount: Mount, tppaStartRequest: TPPAStartRequest) {
        tppaExecutor.execute(camera, mount, tppaStartRequest)
    }

    fun tppaStop(camera: Camera) {
        tppaExecutor.stop(camera)
    }

    fun tppaPause(camera: Camera) {
        tppaExecutor.pause(camera)
    }

    fun tppaUnpause(camera: Camera) {
        tppaExecutor.unpause(camera)
    }
}
