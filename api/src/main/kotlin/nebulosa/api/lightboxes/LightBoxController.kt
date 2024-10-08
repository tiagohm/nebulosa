package nebulosa.api.lightboxes

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.positiveOrZero
import nebulosa.api.javalin.queryParamAsDouble

class LightBoxController(
    app: Javalin,
    private val connectionService: ConnectionService,
    private val lightBoxService: LightBoxService,
) {

    init {
        app.get("light-boxes", ::lightBoxes)
        app.get("light-boxes/{id}", ::lightBox)
        app.put("light-boxes/{id}/connect", ::connect)
        app.put("light-boxes/{id}/disconnect", ::disconnect)
        app.put("light-boxes/{id}/enable", ::enable)
        app.put("light-boxes/{id}/disable", ::disable)
        app.put("light-boxes/{id}/brightness", ::brightness)
        app.put("light-boxes/{id}/listen", ::listen)
    }

    fun lightBoxes(ctx: Context) {
        ctx.json(connectionService.lightBoxes().sorted())
    }

    fun lightBox(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.lightBox(id)?.also(ctx::json)
    }

    fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.connect(lightBox)
    }

    fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.disconnect(lightBox)
    }

    fun enable(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.enable(lightBox)
    }

    fun disable(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.disable(lightBox)
    }

    fun brightness(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        val intensity = ctx.queryParamAsDouble("intensity").positiveOrZero().get()
        lightBoxService.brightness(lightBox, intensity)
    }

    fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.listen(lightBox)
    }
}
