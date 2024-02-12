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

    fun darvStart(camera: Camera, guideOutput: GuideOutput, darvStartRequest: DARVStartRequest): String {
        check(camera.connected) { "camera not connected" }
        check(guideOutput.connected) { "guide output not connected" }
        return darvExecutor.execute(camera, guideOutput, darvStartRequest)
    }

    fun darvStop(id: String) {
        darvExecutor.stop(id)
    }

    fun tppaStart(camera: Camera, mount: Mount, tppaStartRequest: TPPAStartRequest): String {
        check(camera.connected) { "camera not connected" }
        check(mount.connected) { "mount not connected" }
        return tppaExecutor.execute(camera, mount, tppaStartRequest)
    }

    fun tppaStop(id: String) {
        tppaExecutor.stop(id)
    }

    fun tppaPause(id: String) {
        tppaExecutor.pause(id)
    }

    fun tppaUnpause(id: String) {
        tppaExecutor.unpause(id)
    }
}
