package nebulosa.api.mounts

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.*
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.m
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MountController(
    app: Javalin,
    private val connectionService: ConnectionService,
    private val mountService: MountService,
) {

    init {
        app.get("mounts", ::mounts)
        app.get("mount/{id}", ::mount)
        app.put("mount/{id}/connect", ::connect)
        app.put("mount/{id}/disconnect", ::disconnect)
        app.put("mount/{id}/tracking", ::tracking)
        app.put("mount/{id}/sync", ::sync)
        app.put("mount/{id}/slew", ::slew)
        app.put("mount/{id}/goto", ::goTo)
        app.put("mount/{id}/home", ::home)
        app.put("mount/{id}/abort", ::abort)
        app.put("mount/{id}/track-mode", ::trackMode)
        app.put("mount/{id}/slew-rate", ::slewRate)
        app.put("mount/{id}/move", ::move)
        app.put("mount/{id}/park", ::park)
        app.put("mount/{id}/unpark", ::unpark)
        app.put("mount/{id}/coordinates", ::coordinates)
        app.put("mount/{id}/datetime", ::dateTime)
        app.get("mount/{id}/location", ::location)
        app.get("mount/{id}/location/{type}", ::celestialLocation)
        app.put("mount/{id}/point-here", ::pointMountHere)
        app.get("mount/{id}/remote-control", ::remoteControlList)
        app.put("mount/{id}/remote-control/start", ::remoteControlStart)
        app.put("mount/{id}/remote-control/stop", ::remoteControlStop)
        app.put("mount/{id}/listen", ::listen)
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
        val enabled = ctx.queryParamAsBoolean("enabled").get()
        mountService.tracking(mount, enabled)
    }

    private fun sync(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParamAsString("rightAscension").notBlank().get()
        val declination = ctx.queryParamAsString("declination").notBlank().get()
        val j2000 = ctx.queryParamAsBoolean("j2000").getOrDefault(false)
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    private fun slew(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParamAsString("rightAscension").notBlank().get()
        val declination = ctx.queryParamAsString("declination").notBlank().get()
        val j2000 = ctx.queryParamAsBoolean("j2000").getOrDefault(false)
        val idempotencyKey = ctx.idempotencyKey()
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000, idempotencyKey)
    }

    private fun goTo(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rightAscension = ctx.queryParamAsString("rightAscension").notBlank().get()
        val declination = ctx.queryParamAsString("declination").notBlank().get()
        val j2000 = ctx.queryParamAsBoolean("j2000").getOrDefault(false)
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
        val mode = ctx.queryParamAsString("mode").notBlank().get().let(TrackMode::valueOf)
        mountService.trackMode(mount, mode)
    }

    private fun slewRate(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val rate = ctx.queryParamAsString("mode").notBlank().get()
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    private fun move(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val direction = ctx.queryParamAsString("direction").notBlank().get().let(GuideDirection::valueOf)
        val enabled = ctx.queryParamAsBoolean("enabled").get()
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
        val longitude = ctx.queryParamAsString("longitude").notBlank().get()
        val latitude = ctx.queryParamAsString("latitude").notBlank().get()
        val elevation = ctx.queryParamAsDouble("elevation").getOrDefault(0.0)
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    private fun dateTime(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val offsetInMinutes = ctx.queryParamAsInt("offsetInMinutes").range(-720..720).get()
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    private fun celestialLocation(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val type = ctx.pathParamAsString("type").notBlank().get().let(CelestialLocationType::valueOf)

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
        val rightAscension = ctx.queryParamAsString("rightAscension").notBlank().get()
        val declination = ctx.queryParamAsString("declination").notBlank().get()
        val j2000 = ctx.queryParamAsBoolean("j2000").getOrDefault(false)
        val equatorial = ctx.queryParamAsBoolean("equatorial").getOrDefault(true)
        val horizontal = ctx.queryParamAsBoolean("horizontal").getOrDefault(true)
        val meridianAt = ctx.queryParamAsBoolean("meridianAt").getOrDefault(true)
        ctx.json(mountService.computeLocation(mount, rightAscension.hours, declination.deg, j2000, equatorial, horizontal, meridianAt))
    }

    private fun pointMountHere(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val path = ctx.queryParamAsPath("path").exists().get()
        val x = ctx.queryParamAsDouble("x").positiveOrZero().get()
        val y = ctx.queryParamAsDouble("y").positiveOrZero().get()
        mountService.pointMountHere(mount, path, x, y)
    }

    private fun remoteControlStart(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val protocol = ctx.queryParamAsString("procotol").notBlank().get().let(MountRemoteControlProtocol::valueOf)
        val host = ctx.queryParamAsString("host").getOrDefault("0.0.0.0")
        val port = ctx.queryParamAsInt("port").positive().getOrDefault(10001)
        mountService.remoteControlStart(mount, protocol, host, port)
    }

    private fun remoteControlStop(ctx: Context) {
        val id = ctx.pathParam("id")
        val mount = connectionService.mount(id) ?: return
        val protocol = ctx.queryParamAsString("procotol").notBlank().get().let(MountRemoteControlProtocol::valueOf)
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
