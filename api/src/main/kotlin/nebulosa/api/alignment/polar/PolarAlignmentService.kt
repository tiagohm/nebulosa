package nebulosa.api.alignment.polar

import nebulosa.api.alignment.polar.darv.DARVEvent
import nebulosa.api.alignment.polar.darv.DARVExecutor
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAEvent
import nebulosa.api.alignment.polar.tppa.TPPAExecutor
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount

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

    fun darvStatus(camera: Camera): DARVEvent? {
        return darvExecutor.status(camera)
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

    fun tppaStatus(camera: Camera): TPPAEvent? {
        return tppaExecutor.status(camera)
    }
}
