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

    fun tppaStop(camera: Camera, mount: Mount) {
        tppaExecutor.stop(camera, mount)
    }

    fun tppaPause(camera: Camera, mount: Mount) {
        tppaExecutor.pause(camera, mount)
    }

    fun tppaUnpause(camera: Camera, mount: Mount) {
        tppaExecutor.unpause(camera, mount)
    }
}
