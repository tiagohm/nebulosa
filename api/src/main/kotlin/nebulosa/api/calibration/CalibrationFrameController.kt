package nebulosa.api.calibration

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.core.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class CalibrationFrameController(
    override val app: Javalin,
    private val calibrationFrameService: CalibrationFrameService,
) : Controller {

    init {
        app.get("calibration-frames", ::groups)
        app.get("calibration-frames/{group}", ::frames)
        app.put("calibration-frames/{group}", ::upload)
        app.post("calibration-frames", ::update)
        app.delete("calibration-frames/{id}", ::delete)
    }

    private fun groups(ctx: Context) {
        ctx.json(calibrationFrameService.groups())
    }

    private fun frames(ctx: Context) {
        val group = ctx.pathParam("group")
        ctx.json(calibrationFrameService.frames(group).sorted())
    }

    private fun upload(ctx: Context) {
        val group = ctx.pathParam("group")
        val path = ctx.queryParam("path").notNull().path().exists()
        ctx.json(calibrationFrameService.upload(group, path))
    }

    private fun update(ctx: Context) {
        val body = ctx.bodyAsClass<CalibrationFrameEntity>().valid()
        ctx.json(calibrationFrameService.edit(body))
    }

    private fun delete(ctx: Context) {
        val id = ctx.pathParam("id").notNull().toLong()
        calibrationFrameService.delete(id)
    }
}
