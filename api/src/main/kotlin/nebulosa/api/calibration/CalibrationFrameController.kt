package nebulosa.api.calibration

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.path
import nebulosa.api.javalin.valid

class CalibrationFrameController(
    app: Javalin,
    private val calibrationFrameService: CalibrationFrameService,
) {

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
