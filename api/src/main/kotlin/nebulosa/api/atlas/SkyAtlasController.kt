package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import nebulosa.api.beans.annotations.DateAndTimeParam
import nebulosa.api.beans.annotations.EntityParam
import nebulosa.api.locations.LocationEntity
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("sky-atlas")
class SkyAtlasController(
    private val skyAtlasService: SkyAtlasService,
) {

    @GetMapping("sun/image")
    fun imageOfSun(response: HttpServletResponse) {
        skyAtlasService.imageOfSun(response)
    }

    @GetMapping("sun/position")
    fun positionOfSun(
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSun(location, dateTime)

    @GetMapping("sun/altitude-points")
    fun altitudePointsOfSun(
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSun(location, dateTime.toLocalDate(), stepSize)

    @GetMapping("moon/position")
    fun positionOfMoon(
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfMoon(location, dateTime)

    @GetMapping("moon/altitude-points")
    fun altitudePointsOfMoon(
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfMoon(location, dateTime.toLocalDate(), stepSize)

    @GetMapping("planets/{code}/position")
    fun positionOfPlanet(
        @PathVariable code: String,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfPlanet(location, code, dateTime)

    @GetMapping("planets/{code}/altitude-points")
    fun altitudePointsOfPlanet(
        @PathVariable code: String,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfPlanet(location, code, dateTime.toLocalDate(), stepSize)

    @GetMapping("minor-planets")
    fun searchMinorPlanet(@RequestParam @Valid @NotBlank text: String) = skyAtlasService.searchMinorPlanet(text)

    @GetMapping("stars/{star}/position")
    fun positionOfStar(
        @EntityParam star: StarEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfStar(location, star, dateTime)

    @GetMapping("stars/{star}/altitude-points")
    fun altitudePointsOfStar(
        @EntityParam star: StarEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfStar(location, star, dateTime.toLocalDate(), stepSize)

    @GetMapping("stars")
    fun searchStar(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ) = skyAtlasService.searchStar(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("stars/types")
    fun starTypes() = skyAtlasService.starTypes

    @GetMapping("dsos/{dso}/position")
    fun positionOfDSO(
        @EntityParam dso: DeepSkyObjectEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfDSO(location, dso, dateTime)

    @GetMapping("dsos/{dso}/altitude-points")
    fun altitudePointsOfDSO(
        @EntityParam dso: DeepSkyObjectEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfDSO(location, dso, dateTime.toLocalDate(), stepSize)

    @GetMapping("dsos")
    fun searchDSO(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ) = skyAtlasService.searchDSO(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("dsos/types")
    fun dsoTypes() = skyAtlasService.dsoTypes

    @GetMapping("simbad/{id}/position")
    fun positionOfSimbad(
        @PathVariable id: Long,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSimbad(location, id, dateTime)

    @GetMapping("simbad/{id}/altitude-points")
    fun altitudePointsOfSimbad(
        @PathVariable id: Long,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSimbad(location, id, dateTime.toLocalDate(), stepSize)

    @GetMapping("simbad")
    fun searchSimbad(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ) = skyAtlasService.searchSimbad(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("simbad/types")
    fun simbadTypes() = skyAtlasService.simbadTypes

    @GetMapping("satellites/{satellite}/position")
    fun positionOfSatellite(
        @EntityParam satellite: SatelliteEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSatellite(location, satellite, dateTime)

    @GetMapping("satellites/{satellite}/altitude-points")
    fun altitudePointsOfSatellite(
        @EntityParam satellite: SatelliteEntity,
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSatellite(location, satellite, dateTime.toLocalDate(), stepSize)

    @GetMapping("satellites")
    fun searchSatellites(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(name = "group", required = false) groups: List<SatelliteGroupType>?,
    ) = skyAtlasService.searchSatellites(text, groups ?: emptyList())

    @GetMapping("twilight")
    fun twilight(
        @EntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.twilight(location, dateTime.toLocalDate())
}
