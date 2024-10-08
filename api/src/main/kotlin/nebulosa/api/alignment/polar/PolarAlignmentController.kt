package nebulosa.api.alignment.polar

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.validate

class PolarAlignmentController(
    app: Javalin,
    private val polarAlignmentService: PolarAlignmentService,
    private val connectionService: ConnectionService,
) {

    init {
        app.put("polar-alignment/darv/{camera}/{guideOutput}/start", ::darvStart)
        app.put("polar-alignment/darv/{camera}/stop", ::darvStop)
        app.get("polar-alignment/darv/{camera}/status", ::darvStatus)
        app.put("polar-alignment/tppa/{camera}/{mount}/start", ::tppaStart)
        app.put("polar-alignment/tppa/{camera}/stop", ::tppaStop)
        app.put("polar-alignment/tppa/{camera}/pause", ::tppaPause)
        app.put("polar-alignment/tppa/{camera}/unpause", ::tppaUnpause)
        app.get("polar-alignment/tppa/{camera}/status", ::tppaStatus)
    }

    private fun darvStart(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        val guideOutput = connectionService.guideOutput(ctx.pathParam("guideOutput"))!!
        val body = ctx.bodyValidator<DARVStartRequest>().validate().get()
        polarAlignmentService.darvStart(camera, guideOutput, body)
    }

    private fun darvStop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        polarAlignmentService.darvStop(camera)
    }

    private fun darvStatus(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        polarAlignmentService.darvStatus(camera)?.also(ctx::json)
    }

    private fun tppaStart(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        val mount = connectionService.mount(ctx.pathParam("mount"))!!
        val body = ctx.bodyValidator<TPPAStartRequest>().validate().get()
        polarAlignmentService.tppaStart(camera, mount, body)
    }

    private fun tppaStop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        polarAlignmentService.tppaStop(camera)
    }

    private fun tppaPause(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        polarAlignmentService.tppaPause(camera)
    }

    private fun tppaUnpause(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        polarAlignmentService.tppaUnpause(camera)
    }

    private fun tppaStatus(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        polarAlignmentService.tppaStatus(camera)?.also(ctx::json)
    }
}
