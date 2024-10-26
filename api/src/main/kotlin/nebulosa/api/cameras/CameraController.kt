package nebulosa.api.cameras

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.http.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.range
import nebulosa.api.validators.valid

class CameraController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
    private val cameraService: CameraService,
) : Controller {

    init {
        app.get("cameras", ::cameras)
        app.get("cameras/{id}", ::camera)
        app.put("cameras/{id}/connect", ::connect)
        app.put("cameras/{id}/disconnect", ::disconnect)
        app.put("cameras/{id}/snoop", ::snoop)
        app.put("cameras/{id}/cooler", ::cooler)
        app.put("cameras/{id}/temperature/setpoint", ::setpointTemperature)
        app.put("cameras/{id}/capture/start", ::startCapture)
        app.put("cameras/{id}/capture/pause", ::pauseCapture)
        app.put("cameras/{id}/capture/unpause", ::unpauseCapture)
        app.put("cameras/{id}/capture/abort", ::abortCapture)
        app.get("cameras/{id}/capture/status", ::captureStatus)
        app.put("cameras/{id}/listen", ::listen)
    }

    private fun cameras(ctx: Context) {
        ctx.json(connectionService.cameras().sorted())
    }

    private fun camera(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id).notNull()
        ctx.json(camera)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.connect(camera)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.disconnect(camera)
    }

    private fun snoop(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        val mount = ctx.queryParam("mount")?.let(connectionService::mount)
        val wheel = ctx.queryParam("wheel")?.let(connectionService::wheel)
        val focuser = ctx.queryParam("focuser")?.let(connectionService::focuser)
        val rotator = ctx.queryParam("rotator")?.let(connectionService::rotator)
        cameraService.snoop(camera, mount, wheel, focuser, rotator)
    }

    private fun cooler(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        val enabled = ctx.queryParam("enabled").notNull().toBoolean()
        cameraService.cooler(camera, enabled)
    }

    private fun setpointTemperature(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        val temperature = ctx.queryParam("temperature").notNull().toDouble().range(-50.0, 50.0)
        cameraService.setpointTemperature(camera, temperature)
    }

    private fun startCapture(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        val mount = ctx.queryParam("mount")?.let(connectionService::mount)
        val wheel = ctx.queryParam("wheel")?.let(connectionService::wheel)
        val focuser = ctx.queryParam("focuser")?.let(connectionService::focuser)
        val rotator = ctx.queryParam("rotator")?.let(connectionService::rotator)
        val body = ctx.bodyAsClass<CameraStartCaptureRequest>().valid()
        cameraService.startCapture(camera, body, mount, wheel, focuser, rotator)
    }

    private fun pauseCapture(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.pauseCapture(camera)
    }

    private fun unpauseCapture(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.unpauseCapture(camera)
    }

    private fun abortCapture(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.abortCapture(camera)
    }

    private fun captureStatus(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.captureStatus(camera)?.also(ctx::json)
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val camera = connectionService.camera(id) ?: return
        cameraService.listen(camera)
    }
}
