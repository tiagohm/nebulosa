package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.api.beans.converters.time.DateAndTimeParam
import nebulosa.api.locations.LocationEntity
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
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
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSun(location, dateTime)

    @GetMapping("sun/altitude-points")
    fun altitudePointsOfSun(
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
        @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSun(location, dateTime, stepSize)

    @GetMapping("moon/position")
    fun positionOfMoon(
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfMoon(location, dateTime)

    @GetMapping("moon/altitude-points")
    fun altitudePointsOfMoon(
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfMoon(location, dateTime, stepSize)

    @GetMapping("planets/{code}/position")
    fun positionOfPlanet(
        @PathVariable code: String,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfPlanet(location, code, dateTime)

    @GetMapping("planets/{code}/altitude-points")
    fun altitudePointsOfPlanet(
        @PathVariable code: String,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfPlanet(location, code, dateTime, stepSize)

    @GetMapping("minor-planets")
    fun searchMinorPlanet(@RequestParam @Valid @NotBlank text: String) = skyAtlasService.searchMinorPlanet(text)

    @GetMapping("sky-objects/{id}/position")
    fun positionOfSkyObject(
        @PathVariable id: Long,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSkyObject(location, id, dateTime)

    @GetMapping("sky-objects/{id}/altitude-points")
    fun altitudePointsOfSkyObject(
        @PathVariable id: Long,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSkyObject(location, id, dateTime, stepSize)

    @GetMapping("sky-objects")
    fun searchSkyObject(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ) = skyAtlasService.searchSkyObject(
        text, rightAscension.hours, declination.deg, radius.deg,
        constellation, magnitudeMin, magnitudeMax, type,
    )

    @GetMapping("sky-objects/types")
    fun skyObjectTypes() = skyAtlasService.objectTypes

    @GetMapping("satellites/{satellite}/position")
    fun positionOfSatellite(
        @DeviceOrEntityParam satellite: SatelliteEntity,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDateTime,
    ) = skyAtlasService.positionOfSatellite(location, satellite, dateTime)

    @GetMapping("satellites/{satellite}/altitude-points")
    fun altitudePointsOfSatellite(
        @DeviceOrEntityParam satellite: SatelliteEntity,
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ) = skyAtlasService.altitudePointsOfSatellite(location, satellite, dateTime, stepSize)

    @GetMapping("satellites")
    fun searchSatellites(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(name = "group", required = false) groups: List<SatelliteGroupType>?,
    ) = skyAtlasService.searchSatellites(text, groups ?: emptyList())

    @GetMapping("twilight")
    fun twilight(
        @DeviceOrEntityParam location: LocationEntity,
        @DateAndTimeParam dateTime: LocalDate,
    ) = skyAtlasService.twilight(location, dateTime)
}
