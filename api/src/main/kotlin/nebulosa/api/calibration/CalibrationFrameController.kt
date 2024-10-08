package nebulosa.api.calibration

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.pathParamAsLong
import nebulosa.api.javalin.queryParamAsPath

class CalibrationFrameController(
    app: Javalin,
    private val calibrationFrameService: CalibrationFrameService,
) {

    init {
        app.get("calibration-frames", ::groups)
        app.get("calibration-frames/{group}", ::frames)
        app.put("calibration-frames/{group}", ::upload)
        app.post("calibration-frames/{id}", ::update)
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
        val path = ctx.queryParamAsPath("path").exists().get()
        calibrationFrameService.upload(group, path)
    }

    private fun update(ctx: Context) {
        val body = ctx.bodyAsClass<CalibrationFrameEntity>()
        require(body.id > 0L) { "invalid frame id" }
        ctx.json(calibrationFrameService.edit(body))
    }

    private fun delete(ctx: Context) {
        val id = ctx.pathParamAsLong("id").get()
        calibrationFrameService.delete(id)
    }
}
