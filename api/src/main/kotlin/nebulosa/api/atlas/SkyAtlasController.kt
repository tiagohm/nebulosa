package nebulosa.api.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.ktor.Controller
import nebulosa.api.ktor.location
import nebulosa.api.validators.*
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import java.time.LocalDateTime

class SkyAtlasController(
    override val app: Application,
    private val skyAtlasService: SkyAtlasService,
    private val satelliteRepository: SatelliteRepository,
    private val mapper: ObjectMapper,
) : Controller {

    init {
        with(app) {
            routing {
                get("/sky-atlas/sun/image", ::imageOfSun)
                get("/sky-atlas/sun/position", ::positionOfSun)
                get("/sky-atlas/sun/altitude-points", ::altitudePointsOfSun)
                get("/sky-atlas/moon/position", ::positionOfMoon)
                get("/sky-atlas/moon/altitude-points", ::altitudePointsOfMoon)
                get("/sky-atlas/moon/phase", ::moonPhase)
                get("/sky-atlas/twilight", ::twilight)
                get("/sky-atlas/planets/{code}/position", ::positionOfPlanet)
                get("/sky-atlas/planets/{code}/altitude-points", ::altitudePointsOfPlanet)
                get("/sky-atlas/minor-planets", ::searchMinorPlanet)
                get("/sky-atlas/minor-planets/close-approaches", ::closeApproachesForMinorPlanets)
                get("/sky-atlas/sky-objects", ::searchSkyObject)
                get("/sky-atlas/sky-objects/types", ::skyObjectTypes)
                get("/sky-atlas/sky-objects/{id}/position", ::positionOfSkyObject)
                get("/sky-atlas/sky-objects/{id}/altitude-points", ::altitudePointsOfSkyObject)
                get("/sky-atlas/satellites", ::searchSatellites)
                get("/sky-atlas/satellites/{id}/position", ::positionOfSatellite)
                get("/sky-atlas/satellites/{id}/altitude-points", ::altitudePointsOfSatellite)
            }
        }
    }

    private suspend fun imageOfSun(ctx: RoutingContext) = with(ctx.call) {
        respondBytes(ContentType.Image.PNG) { skyAtlasService.imageOfSun() }
    }

    private suspend fun positionOfSun(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.positionOfSun(location, dateTime, fast))
    }

    private suspend fun altitudePointsOfSun(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val stepSize = queryParameters[STEP_SIZE]?.toInt()?.min(1) ?: 1
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.altitudePointsOfSun(location, dateTime, stepSize, fast))
    }

    private suspend fun positionOfMoon(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.positionOfMoon(location, dateTime, fast))
    }

    private suspend fun altitudePointsOfMoon(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val stepSize = queryParameters[STEP_SIZE]?.toInt()?.min(1) ?: 1
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.altitudePointsOfMoon(location, dateTime, stepSize, fast))
    }

    private suspend fun positionOfPlanet(ctx: RoutingContext) = with(ctx.call) {
        val code = pathParameters[CODE].notNull()
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.positionOfPlanet(location, code, dateTime, fast))
    }

    private suspend fun altitudePointsOfPlanet(ctx: RoutingContext) = with(ctx.call) {
        val code = pathParameters[CODE].notNull()
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val stepSize = queryParameters[STEP_SIZE]?.toInt()?.min(1) ?: 1
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.altitudePointsOfPlanet(location, code, dateTime, stepSize, fast))
    }

    private suspend fun searchMinorPlanet(ctx: RoutingContext) = with(ctx.call) {
        val text = queryParameters[TEXT].notNullOrBlank()
        respond(skyAtlasService.searchMinorPlanet(text))
    }

    private suspend fun closeApproachesForMinorPlanets(ctx: RoutingContext) = with(ctx.call) {
        val days = queryParameters[DAYS]?.toLong()?.positive() ?: 7L
        val distance = queryParameters[DISTANCE]?.toDouble()?.positive() ?: 10.0
        val date = queryParameters[DATE]?.localDate()
        respond(skyAtlasService.closeApproachesForMinorPlanets(days, distance, date))
    }

    private suspend fun positionOfSkyObject(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull().toLong().positive()
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        respond(skyAtlasService.positionOfSkyObject(location, id, dateTime))
    }

    private suspend fun altitudePointsOfSkyObject(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull().toLong().positive()
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val stepSize = queryParameters[STEP_SIZE]?.toInt()?.min(1) ?: 1
        respond(skyAtlasService.altitudePointsOfSkyObject(location, id, dateTime, stepSize))
    }

    private suspend fun searchSkyObject(ctx: RoutingContext) = with(ctx.call) {
        val text = queryParameters[TEXT] ?: ""
        val rightAscension = queryParameters["rightAscension"] ?: ""
        val declination = queryParameters["declination"] ?: ""
        val radius = queryParameters["radius"]?.toDouble() ?: 0.0
        val constellation = queryParameters["constellation"]?.enumOf<Constellation>()
        val magnitudeMin = queryParameters["magnitudeMin"]?.toDouble() ?: SkyObject.MAGNITUDE_MIN
        val magnitudeMax = queryParameters["magnitudeMax"]?.toDouble() ?: SkyObject.MAGNITUDE_MAX
        val type = queryParameters[TYPE]?.enumOf<SkyObjectType>()
        val id = queryParameters[ID]?.toLong() ?: 0L

        val result = skyAtlasService.searchSkyObject(
            text, rightAscension.hours, declination.deg, radius.deg,
            constellation, magnitudeMin, magnitudeMax, type, id,
        )

        respond(result)
    }

    private suspend fun skyObjectTypes(ctx: RoutingContext) = with(ctx.call) {
        respond(skyAtlasService.objectTypes)
    }

    private suspend fun positionOfSatellite(ctx: RoutingContext) = with(ctx.call) {
        val satellite = satelliteRepository[pathParameters[ID].notNull().toLong().positive()] ?: return
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        respond(skyAtlasService.positionOfSatellite(location, satellite, dateTime))
    }

    private suspend fun altitudePointsOfSatellite(ctx: RoutingContext) = with(ctx.call) {
        val satellite = satelliteRepository[pathParameters[ID].notNull().toLong().positive()] ?: return
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val stepSize = queryParameters[STEP_SIZE]?.toInt()?.min(1) ?: 1
        respond(skyAtlasService.altitudePointsOfSatellite(location, satellite, dateTime, stepSize))
    }

    private suspend fun searchSatellites(ctx: RoutingContext) = with(ctx.call) {
        val text = queryParameters[TEXT] ?: ""
        val id = queryParameters[ID]?.toLong() ?: 0L
        val groups = queryParameters.getAll(GROUP)?.map(SatelliteGroupType::valueOf) ?: emptyList()
        respond(skyAtlasService.searchSatellites(text, groups, id))
    }

    private suspend fun twilight(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        val fast = queryParameters[FAST]?.toBoolean() == true
        respond(skyAtlasService.twilight(location, dateTime, fast))
    }

    private suspend fun moonPhase(ctx: RoutingContext) = with(ctx.call) {
        val location = location(mapper).notNull()
        val date = queryParameters[DATE].notNull().localDate()
        val time = queryParameters[TIME].notNull().localTime()
        val dateTime = LocalDateTime.of(date, time)
        respondNullable(skyAtlasService.moonPhase(location, dateTime))
    }

    companion object {

        private const val ID = "id"
        private const val DATE = "date"
        private const val TIME = "time"
        private const val FAST = "fast"
        private const val CODE = "code"
        private const val TEXT = "text"
        private const val TYPE = "type"
        private const val DAYS = "days"
        private const val GROUP = "group"
        private const val DISTANCE = "distance"
        private const val STEP_SIZE = "stepSize"
    }
}
