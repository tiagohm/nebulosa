package nebulosa.api.mounts

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.ktor.idempotencyKey
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
    override val server: Application,
    private val connectionService: ConnectionService,
    private val mountService: MountService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/mounts", ::mounts)
                get("/mounts/{id}", ::mount)
                put("/mounts/{id}/connect", ::connect)
                put("/mounts/{id}/disconnect", ::disconnect)
                put("/mounts/{id}/tracking", ::tracking)
                put("/mounts/{id}/sync", ::sync)
                put("/mounts/{id}/slew", ::slew)
                put("/mounts/{id}/goto", ::goTo)
                put("/mounts/{id}/home", ::home)
                put("/mounts/{id}/abort", ::abort)
                put("/mounts/{id}/track-mode", ::trackMode)
                put("/mounts/{id}/slew-rate", ::slewRate)
                put("/mounts/{id}/move", ::move)
                put("/mounts/{id}/park", ::park)
                put("/mounts/{id}/unpark", ::unpark)
                put("/mounts/{id}/coordinates", ::coordinates)
                put("/mounts/{id}/datetime", ::dateTime)
                get("/mounts/{id}/location", ::location)
                get("/mounts/{id}/location/{type}", ::celestialLocation)
                put("/mounts/{id}/point-here", ::pointMountHere)
                get("/mounts/{id}/remote-control", ::remoteControlList)
                put("/mounts/{id}/remote-control/start", ::remoteControlStart)
                put("/mounts/{id}/remote-control/stop", ::remoteControlStop)
                put("/mounts/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun mounts(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.mounts().sorted())
    }

    private suspend fun mount(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.mount(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.connect(mount)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.disconnect(mount)
    }

    private fun tracking(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val enabled = queryParameters["enabled"].notNull().toBoolean()
        mountService.tracking(mount, enabled)
    }

    private fun sync(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val rightAscension = queryParameters["rightAscension"].notNullOrBlank()
        val declination = queryParameters["declination"].notNullOrBlank()
        val j2000 = queryParameters["j2000"]?.toBoolean() == true
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    private fun slew(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val rightAscension = queryParameters["rightAscension"].notNullOrBlank()
        val declination = queryParameters["declination"].notNullOrBlank()
        val j2000 = queryParameters["j2000"]?.toBoolean() == true
        val idempotencyKey = idempotencyKey()
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000, idempotencyKey)
    }

    private fun goTo(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val rightAscension = queryParameters["rightAscension"].notNullOrBlank()
        val declination = queryParameters["declination"].notNullOrBlank()
        val j2000 = queryParameters["j2000"]?.toBoolean() == true
        val idempotencyKey = idempotencyKey()
        mountService.goTo(mount, rightAscension.hours, declination.deg, j2000, idempotencyKey)
    }

    private fun home(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.home(mount)
    }

    private fun abort(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.abort(mount)
    }

    private fun trackMode(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val mode = queryParameters["mode"].notNullOrBlank().enumOf<TrackMode>()
        mountService.trackMode(mount, mode)
    }

    private fun slewRate(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val rate = queryParameters["rate"].notNullOrBlank()
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    private fun move(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val direction = queryParameters["direction"].notNullOrBlank().enumOf<GuideDirection>()
        val enabled = queryParameters["enabled"].notNull().toBoolean()
        mountService.move(mount, direction, enabled)
    }

    private fun park(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.park(mount)
    }

    private fun unpark(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.unpark(mount)
    }

    private fun coordinates(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val longitude = queryParameters["longitude"].notNullOrBlank()
        val latitude = queryParameters["latitude"].notNullOrBlank()
        val elevation = queryParameters["elevation"]?.toDouble() ?: 0.0
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    private fun dateTime(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val date = queryParameters["date"].notNull().localDate()
        val time = queryParameters["time"].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val offsetInMinutes = queryParameters["offsetInMinutes"].notNull().toInt().range(-720, 720)
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    private suspend fun celestialLocation(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val type = pathParameters["type"].notNullOrBlank().enumOf<CelestialLocationType>()

        val location = when (type) {
            CelestialLocationType.ZENITH -> mountService.computeZenithLocation(mount)
            CelestialLocationType.NORTH_POLE -> mountService.computeNorthCelestialPoleLocation(mount)
            CelestialLocationType.SOUTH_POLE -> mountService.computeSouthCelestialPoleLocation(mount)
            CelestialLocationType.GALACTIC_CENTER -> mountService.computeGalacticCenterLocation(mount)
            CelestialLocationType.MERIDIAN_EQUATOR -> mountService.computeMeridianEquatorLocation(mount)
            CelestialLocationType.MERIDIAN_ECLIPTIC -> mountService.computeMeridianEclipticLocation(mount)
            CelestialLocationType.EQUATOR_ECLIPTIC -> mountService.computeEquatorEclipticLocation(mount)
        }

        respond(location)
    }

    private suspend fun location(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val rightAscension = queryParameters["rightAscension"].notNullOrBlank()
        val declination = queryParameters["declination"].notNullOrBlank()
        val j2000 = queryParameters["j2000"]?.toBoolean() == true
        val equatorial = queryParameters["equatorial"]?.toBoolean() != false
        val horizontal = queryParameters["horizontal"]?.toBoolean() != false
        val meridianAt = queryParameters["meridianAt"]?.toBoolean() != false
        respond(mountService.computeLocation(mount, rightAscension.hours, declination.deg, j2000, equatorial, horizontal, meridianAt))
    }

    private fun pointMountHere(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val path = queryParameters["path"].notNull().path().exists()
        val x = queryParameters["x"].notNull().toDouble().positiveOrZero()
        val y = queryParameters["y"].notNull().toDouble().positiveOrZero()
        mountService.pointMountHere(mount, path, x, y)
    }

    private fun remoteControlStart(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val protocol = queryParameters["protocol"].notNullOrBlank().enumOf<MountRemoteControlProtocol>()
        val host = queryParameters["host"]?.ifBlank { null } ?: "0.0.0.0"
        val port = queryParameters["port"]?.toInt()?.positive() ?: 10001
        mountService.remoteControlStart(mount, protocol, host, port)
    }

    private fun remoteControlStop(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        val protocol = queryParameters["protocol"].notNullOrBlank().enumOf<MountRemoteControlProtocol>()
        mountService.remoteControlStop(mount, protocol)
    }

    private suspend fun remoteControlList(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        respond(mountService.remoteControlList(mount))
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val mount = connectionService.mount(id) ?: return
        mountService.listen(mount)
    }

    companion object {

        private const val ID = "id"
    }
}
