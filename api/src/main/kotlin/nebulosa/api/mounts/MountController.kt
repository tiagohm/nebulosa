package nebulosa.api.mounts

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.core.Controller
import nebulosa.api.core.idempotencyKey
import nebulosa.api.validators.*
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.m
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MountController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
    private val mountService: MountService,
) : Controller {

    init {
        app.get("mounts", ::mounts)
        app.get("mounts/{id}", ::mount)
        app.put("mounts/{id}/connect", ::connect)
        app.put("mounts/{id}/disconnect", ::disconnect)
        app.put("mounts/{id}/tracking", ::tracking)
        app.put("mounts/{id}/sync", ::sync)
        app.put("mounts/{id}/slew", ::slew)
        app.put("mounts/{id}/goto", ::goTo)
        app.put("mounts/{id}/home", ::home)
        app.put("mounts/{id}/abort", ::abort)
        app.put("mounts/{id}/track-mode", ::trackMode)
        app.put("mounts/{id}/slew-rate", ::slewRate)
        app.put("mounts/{id}/move", ::move)
        app.put("mounts/{id}/park", ::park)
        app.put("mounts/{id}/unpark", ::unpark)
        app.put("mounts/{id}/coordinates", ::coordinates)
        app.put("mounts/{id}/datetime", ::dateTime)
        app.get("mounts/{id}/location", ::location)
        app.get("mounts/{id}/location/{type}", ::celestialLocation)
        app.put("mounts/{id}/point-here", ::pointMountHere)
        app.get("mounts/{id}/remote-control", ::remoteControlList)
        app.put("mounts/{id}/remote-control/start", ::remoteControlStart)
        app.put("mounts/{id}/remote-control/stop", ::remoteControlStop)
        app.put("mounts/{id}/listen", ::listen)
    }

    private fun mounts(ctx: Context) {
        ctx.json(connectionService.mounts().sorted())
    }

    private fun mount(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.mount(id)?.also(ctx::json)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.connect(mount)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.disconnect(mount)
    }

    private fun tracking(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val enabled = ctx.queryParam("enabled").notNull().toBoolean()
        mountService.tracking(mount, enabled)
    }

    private fun sync(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParam("rightAscension").notNullOrBlank()
        val declination = ctx.queryParam("declination").notNullOrBlank()
        val j2000 = ctx.queryParam("j2000")?.toBoolean() ?: false
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    private fun slew(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParam("rightAscension").notNullOrBlank()
        val declination = ctx.queryParam("declination").notNullOrBlank()
        val j2000 = ctx.queryParam("j2000")?.toBoolean() ?: false
        val idempotencyKey = ctx.idempotencyKey()
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000, idempotencyKey)
    }

    private fun goTo(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParam("rightAscension").notNullOrBlank()
        val declination = ctx.queryParam("declination").notNullOrBlank()
        val j2000 = ctx.queryParam("j2000")?.toBoolean() ?: false
        val idempotencyKey = ctx.idempotencyKey()
        mountService.goTo(mount, rightAscension.hours, declination.deg, j2000, idempotencyKey)
    }

    private fun home(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.home(mount)
    }

    private fun abort(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.abort(mount)
    }

    private fun trackMode(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val mode = ctx.queryParam("mode").notNullOrBlank().enumOf<TrackMode>()
        mountService.trackMode(mount, mode)
    }

    private fun slewRate(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rate = ctx.queryParam("rate").notNullOrBlank()
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    private fun move(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val direction = ctx.queryParam("direction").notNullOrBlank().enumOf<GuideDirection>()
        val enabled = ctx.queryParam("enabled").notNull().toBoolean()
        mountService.move(mount, direction, enabled)
    }

    private fun park(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.park(mount)
    }

    private fun unpark(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.unpark(mount)
    }

    private fun coordinates(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val longitude = ctx.queryParam("longitude").notNullOrBlank()
        val latitude = ctx.queryParam("latitude").notNullOrBlank()
        val elevation = ctx.queryParam("elevation")?.toDouble() ?: 0.0
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    private fun dateTime(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val date = ctx.queryParam("date").notNull().localDate()
        val time = ctx.queryParam("time").notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val offsetInMinutes = ctx.queryParam("offsetInMinutes").notNull().toInt().range(-720, 720)
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    private fun celestialLocation(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val type = ctx.pathParam("type").notNullOrBlank().enumOf<CelestialLocationType>()

        val location = when (type) {
            CelestialLocationType.ZENITH -> mountService.computeZenithLocation(mount)
            CelestialLocationType.NORTH_POLE -> mountService.computeNorthCelestialPoleLocation(mount)
            CelestialLocationType.SOUTH_POLE -> mountService.computeSouthCelestialPoleLocation(mount)
            CelestialLocationType.GALACTIC_CENTER -> mountService.computeGalacticCenterLocation(mount)
            CelestialLocationType.MERIDIAN_EQUATOR -> mountService.computeMeridianEquatorLocation(mount)
            CelestialLocationType.MERIDIAN_ECLIPTIC -> mountService.computeMeridianEclipticLocation(mount)
            CelestialLocationType.EQUATOR_ECLIPTIC -> mountService.computeEquatorEclipticLocation(mount)
        }

        ctx.json(location)
    }

    private fun location(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParam("rightAscension").notNullOrBlank()
        val declination = ctx.queryParam("declination").notNullOrBlank()
        val j2000 = ctx.queryParam("j2000")?.toBoolean() ?: false
        val equatorial = ctx.queryParam("equatorial")?.toBoolean() ?: true
        val horizontal = ctx.queryParam("horizontal")?.toBoolean() ?: true
        val meridianAt = ctx.queryParam("meridianAt")?.toBoolean() ?: true
        ctx.json(mountService.computeLocation(mount, rightAscension.hours, declination.deg, j2000, equatorial, horizontal, meridianAt))
    }

    private fun pointMountHere(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val path = ctx.queryParam("path").notNull().path().exists()
        val x = ctx.queryParam("x").notNull().toDouble().positiveOrZero()
        val y = ctx.queryParam("y").notNull().toDouble().positiveOrZero()
        mountService.pointMountHere(mount, path, x, y)
    }

    private fun remoteControlStart(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val protocol = ctx.queryParam("protocol").notNullOrBlank().enumOf<MountRemoteControlProtocol>()
        val host = ctx.queryParam("host")?.ifBlank { null } ?: "0.0.0.0"
        val port = ctx.queryParam("port")?.toInt()?.positive() ?: 10001
        mountService.remoteControlStart(mount, protocol, host, port)
    }

    private fun remoteControlStop(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val protocol = ctx.queryParam("protocol").notNullOrBlank().enumOf<MountRemoteControlProtocol>()
        mountService.remoteControlStop(mount, protocol)
    }

    private fun remoteControlList(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        ctx.json(mountService.remoteControlList(mount))
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        mountService.listen(mount)
    }
}
