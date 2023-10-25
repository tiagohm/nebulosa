package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import nebulosa.api.beans.annotations.DateAndTime
import nebulosa.api.beans.annotations.EntityBy
import nebulosa.api.locations.LocationEntity
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("sky-atlas")
class AtlasController(
    private val atlasService: AtlasService,
) {

    @GetMapping("sun/image")
    fun imageOfSun(response: HttpServletResponse) {
        atlasService.imageOfSun(response)
    }

    @GetMapping("sun/position")
    fun positionOfSun(
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfSun(location, dateTime)

    @GetMapping("sun/altitude-points")
    fun altitudePointsOfSun(
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) stepSize: Int,
    ) = atlasService.altitudePointsOfSun(location, dateTime.toLocalDate(), stepSize)

    @GetMapping("moon/position")
    fun positionOfMoon(
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfMoon(location, dateTime)

    @GetMapping("moon/altitude-points")
    fun altitudePointsOfMoon(
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfMoon(location, dateTime.toLocalDate(), stepSize)

    @GetMapping("planets/{code}/position")
    fun positionOfPlanet(
        @PathVariable code: String,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfPlanet(location, code, dateTime)

    @GetMapping("planets/{code}/altitude-points")
    fun altitudePointsOfPlanet(
        @PathVariable code: String,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfPlanet(location, code, dateTime.toLocalDate(), stepSize)

    @GetMapping("minor-planets")
    fun searchMinorPlanet(@RequestParam @Valid @NotBlank text: String) = atlasService.searchMinorPlanet(text)

    @GetMapping("stars/{star}/position")
    fun positionOfStar(
        @EntityBy star: StarEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfStar(location, star, dateTime)

    @GetMapping("stars/{star}/altitude-points")
    fun altitudePointsOfStar(
        @EntityBy star: StarEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfStar(location, star, dateTime.toLocalDate(), stepSize)

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
    ) = atlasService.searchStar(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("stars/types")
    fun starTypes() = atlasService.starTypes

    @GetMapping("dsos/{dso}/position")
    fun positionOfDSO(
        @EntityBy dso: DeepSkyObjectEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfDSO(location, dso, dateTime)

    @GetMapping("dsos/{dso}/altitude-points")
    fun altitudePointsOfDSO(
        @EntityBy dso: DeepSkyObjectEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfDSO(location, dso, dateTime.toLocalDate(), stepSize)

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
    ) = atlasService.searchDSO(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("dsos/types")
    fun dsoTypes() = atlasService.dsoTypes

    @GetMapping("simbad/{id}/position")
    fun positionOfSimbad(
        @PathVariable id: Long,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfSimbad(location, id, dateTime)

    @GetMapping("simbad/{id}/altitude-points")
    fun altitudePointsOfSimbad(
        @PathVariable id: Long,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfSimbad(location, id, dateTime.toLocalDate(), stepSize)

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
    ) = atlasService.searchSimbad(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("simbad/types")
    fun simbadTypes() = atlasService.simbadTypes

    @GetMapping("satellites/{satellite}/position")
    fun positionOfSatellite(
        @EntityBy satellite: SatelliteEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.positionOfSatellite(location, satellite, dateTime)

    @GetMapping("satellites/{satellite}/altitude-points")
    fun altitudePointsOfSatellite(
        @EntityBy satellite: SatelliteEntity,
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = atlasService.altitudePointsOfSatellite(location, satellite, dateTime.toLocalDate(), stepSize)

    @GetMapping("satellites")
    fun searchSatellites(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(name = "group", required = false) groups: List<SatelliteGroupType>?,
    ) = atlasService.searchSatellites(text, groups ?: emptyList())

    @GetMapping("twilight")
    fun twilight(
        @EntityBy location: LocationEntity,
        @DateAndTime dateTime: LocalDateTime,
    ) = atlasService.twilight(location, dateTime.toLocalDate())
}
