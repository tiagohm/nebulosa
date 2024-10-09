package nebulosa.api.atlas

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.queryParamsAsClass
import nebulosa.api.javalin.*
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import java.time.LocalDateTime

class SkyAtlasController(
    app: Javalin,
    private val skyAtlasService: SkyAtlasService,
    private val satelliteRepository: SatelliteRepository,
) {

    init {
        app.get("sky-atlas/sun/image", ::imageOfSun)
        app.get("sky-atlas/sun/position", ::positionOfSun)
        app.get("sky-atlas/sun/altitude-points", ::altitudePointsOfSun)
        app.get("sky-atlas/moon/position", ::positionOfMoon)
        app.get("sky-atlas/moon/altitude-points", ::altitudePointsOfMoon)
        app.get("sky-atlas/moon/phase", ::moonPhase)
        app.get("sky-atlas/twilight", ::twilight)
        app.get("sky-atlas/planets/{code}/position", ::positionOfPlanet)
        app.get("sky-atlas/planets/{code}/altitude-points", ::altitudePointsOfPlanet)
        app.get("sky-atlas/minor-planets", ::searchMinorPlanet)
        app.get("sky-atlas/minor-planets/close-approaches", ::closeApproachesForMinorPlanets)
        app.get("sky-atlas/sky-objects", ::searchSkyObject)
        app.get("sky-atlas/sky-objects/types", ::skyObjectTypes)
        app.get("sky-atlas/sky-objects/{id}/position", ::positionOfSkyObject)
        app.get("sky-atlas/sky-objects/{id}/altitude-points", ::altitudePointsOfSkyObject)
        app.get("sky-atlas/satellites/", ::searchSatellites)
        app.get("sky-atlas/satellites/{id}/position", ::positionOfSatellite)
        app.get("sky-atlas/satellites/{id}/altitude-points", ::altitudePointsOfSatellite)
    }

    private fun imageOfSun(ctx: Context) {
        skyAtlasService.imageOfSun(ctx.res())
    }

    private fun positionOfSun(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.positionOfSun(location, dateTime, fast))
    }

    private fun altitudePointsOfSun(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val stepSize = ctx.queryParamAsInt("stepSize").min(1).getOrDefault(1)
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.altitudePointsOfSun(location, dateTime, stepSize, fast))
    }

    private fun positionOfMoon(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.positionOfMoon(location, dateTime, fast))
    }

    private fun altitudePointsOfMoon(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val stepSize = ctx.queryParamAsInt("stepSize").min(1).getOrDefault(1)
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.altitudePointsOfMoon(location, dateTime, stepSize, fast))
    }

    private fun positionOfPlanet(ctx: Context) {
        val code = ctx.pathParam("code")
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.positionOfPlanet(location, code, dateTime, fast))
    }

    private fun altitudePointsOfPlanet(ctx: Context) {
        val code = ctx.pathParam("code")
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val stepSize = ctx.queryParamAsInt("stepSize").min(1).getOrDefault(1)
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.altitudePointsOfPlanet(location, code, dateTime, stepSize, fast))
    }

    private fun searchMinorPlanet(ctx: Context) {
        val text = ctx.queryParamAsString("text").notBlank().get()
        ctx.json(skyAtlasService.searchMinorPlanet(text))
    }

    private fun closeApproachesForMinorPlanets(ctx: Context) {
        val days = ctx.queryParamAsLong("days").positive().getOrDefault(7L)
        val distance = ctx.queryParamAsDouble("distance").positive().getOrDefault(10.0)
        val date = ctx.localDateOrNull()
        ctx.json(skyAtlasService.closeApproachesForMinorPlanets(days, distance, date))
    }

    private fun positionOfSkyObject(ctx: Context) {
        val id = ctx.pathParamAsLong("id").positive().get()
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        ctx.json(skyAtlasService.positionOfSkyObject(location, id, dateTime))
    }

    private fun altitudePointsOfSkyObject(ctx: Context) {
        val id = ctx.pathParamAsLong("id").positive().get()
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val stepSize = ctx.queryParamAsInt("stepSize").min(1).getOrDefault(1)
        ctx.json(skyAtlasService.altitudePointsOfSkyObject(location, id, dateTime, stepSize))
    }

    private fun searchSkyObject(ctx: Context) {
        val text = ctx.queryParamAsString("text").getOrDefault("")
        val rightAscension = ctx.queryParamAsString("rightAscension").getOrDefault("")
        val declination = ctx.queryParamAsString("declination").getOrDefault("")
        val radius = ctx.queryParamAsDouble("radius").getOrDefault(0.0)
        val constellation = ctx.queryParamAsString("constellation").allowNullable().get()?.let(Constellation::valueOf)
        val magnitudeMin = ctx.queryParamAsDouble("magnitudeMin").getOrDefault(SkyObject.MAGNITUDE_MIN)
        val magnitudeMax = ctx.queryParamAsDouble("magnitudeMax").getOrDefault(SkyObject.MAGNITUDE_MAX)
        val type = ctx.queryParamAsString("type").allowNullable().get()?.let(SkyObjectType::valueOf)
        val id = ctx.queryParamAsLong("id").getOrDefault(0L)

        val result = skyAtlasService.searchSkyObject(
            text, rightAscension.hours, declination.deg, radius.deg,
            constellation, magnitudeMin, magnitudeMax, type, id,
        )

        ctx.json(result)
    }

    private fun skyObjectTypes(ctx: Context) {
        ctx.json(skyAtlasService.objectTypes)
    }

    private fun positionOfSatellite(ctx: Context) {
        val satellite = satelliteRepository.find(ctx.pathParamAsLong("id").get()) ?: return
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        ctx.json(skyAtlasService.positionOfSatellite(location, satellite, dateTime))
    }

    private fun altitudePointsOfSatellite(ctx: Context) {
        val satellite = satelliteRepository.find(ctx.pathParamAsLong("id").get()) ?: return
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val stepSize = ctx.queryParamAsInt("stepSize").min(1).getOrDefault(1)
        ctx.json(skyAtlasService.altitudePointsOfSatellite(location, satellite, dateTime, stepSize))
    }

    private fun searchSatellites(ctx: Context) {
        val text = ctx.queryParamAsString("text").getOrDefault("")
        val id = ctx.queryParamAsLong("id").getOrDefault(0L)
        val groups = ctx.queryParamsAsClass<String>("groups").allowNullable().get()?.map(SatelliteGroupType::valueOf)
        ctx.json(skyAtlasService.searchSatellites(text, groups ?: emptyList(), id))
    }

    private fun twilight(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        val fast = ctx.queryParamAsBoolean("fast").getOrDefault(false)
        ctx.json(skyAtlasService.twilight(location, dateTime, fast))
    }

    private fun moonPhase(ctx: Context) {
        val location = ctx.location()
        val dateTime = LocalDateTime.of(ctx.localDate(), ctx.localTime())
        skyAtlasService.moonPhase(location, dateTime)?.also(ctx::json)
    }
}
