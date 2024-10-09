package nebulosa.api.wizard.flat

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.valid

class FlatWizardController(
    app: Javalin,
    private val flatWizardService: FlatWizardService,
    private val connectionService: ConnectionService,
) {

    init {
        app.put("flat-wizard/{camera}/start", ::start)
        app.put("flat-wizard/{camera}/stop", ::stop)
        app.get("flat-wizard/{camera}/status", ::status)
    }

    private fun start(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        val body = ctx.bodyAsClass<FlatWizardRequest>().valid()
        flatWizardService.start(camera, body)
    }

    private fun stop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        flatWizardService.stop(camera)
    }

    private fun status(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        flatWizardService.status(camera)?.also(ctx::json)
    }
}
