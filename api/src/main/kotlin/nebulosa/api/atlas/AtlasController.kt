package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import nebulosa.api.data.entities.DeepSkyObjectEntity
import nebulosa.api.data.entities.StarEntity
import nebulosa.api.data.enums.SatelliteGroupType
import nebulosa.api.data.responses.BodyPositionResponse
import nebulosa.api.data.responses.MinorPlanetResponse
import nebulosa.api.data.responses.SatelliteResponse
import nebulosa.api.data.responses.TwilightResponse
import nebulosa.api.repositories.DeepSkyObjectRepository
import nebulosa.api.repositories.LocationRepository
import nebulosa.api.repositories.SatelliteRepository
import nebulosa.api.repositories.StarRepository
import nebulosa.api.utils.noSeconds
import nebulosa.api.utils.orNow
import nebulosa.api.utils.plus
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime

@RestController
@RequestMapping("sky-atlas")
class AtlasController(
    private val atlasService: AtlasService,
    private val locationRepository: LocationRepository,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val satelliteRepository: SatelliteRepository,
) {

    @GetMapping("sun/image")
    fun imageOfSun(response: HttpServletResponse) {
        atlasService.imageOfSun(response)
    }

    @GetMapping("sun/position")
    fun positionOfSun(
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfSun(locationRepository.withId(location)!!, (date + time).noSeconds())
    }

    @GetMapping("sun/altitude-points")
    fun altitudePointsOfSun(
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfSun(locationRepository.withId(location)!!, date.orNow(), stepSize)
    }

    @GetMapping("moon/position")
    fun positionOfMoon(
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfMoon(locationRepository.withId(location)!!, (date + time).noSeconds())
    }

    @GetMapping("moon/altitude-points")
    fun altitudePointsOfMoon(
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfMoon(locationRepository.withId(location)!!, date.orNow(), stepSize)
    }

    @GetMapping("planets/{code}/position")
    fun positionOfPlanet(
        @PathVariable code: String,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfPlanet(locationRepository.withId(location)!!, code, (date + time).noSeconds())
    }

    @GetMapping("planets/{code}/altitude-points")
    fun altitudePointsOfPlanet(
        @PathVariable code: String,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfPlanet(locationRepository.withId(location)!!, code, date.orNow(), stepSize)
    }

    @GetMapping("minor-planets")
    fun searchMinorPlanet(@RequestParam @Valid @NotBlank text: String): MinorPlanetResponse {
        return atlasService.searchMinorPlanet(text)
    }

    @GetMapping("stars/{id}/position")
    fun positionOfStar(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfStar(locationRepository.withId(location)!!, starRepository.withId(id)!!, (date + time).noSeconds())
    }

    @GetMapping("stars/{id}/altitude-points")
    fun altitudePointsOfStar(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService
            .altitudePointsOfStar(locationRepository.withId(location)!!, starRepository.withId(id)!!, date.orNow(), stepSize)
    }

    @GetMapping("stars")
    fun searchStar(
        @RequestParam @Valid @NotBlank text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ): List<StarEntity> {
        return atlasService.searchStar(
            text, rightAscension.hours, declination.deg, radius.deg,
            constellation, magnitudeMin, magnitudeMax, type,
        )
    }

    @GetMapping("dsos/{id}/position")
    fun positionOfDSO(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfDSO(locationRepository.withId(location)!!, deepSkyObjectRepository.withId(id)!!, (date + time).noSeconds())
    }

    @GetMapping("dsos/{id}/altitude-points")
    fun altitudePointsOfDSO(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService
            .altitudePointsOfDSO(locationRepository.withId(location)!!, deepSkyObjectRepository.withId(id)!!, date.orNow(), stepSize)
    }

    @GetMapping("dsos")
    fun searchDSO(
        @RequestParam @Valid @NotBlank text: String,
        @RequestParam(required = false, defaultValue = "") rightAscension: String,
        @RequestParam(required = false, defaultValue = "") declination: String,
        @RequestParam(required = false, defaultValue = "0.0") radius: Double,
        @RequestParam(required = false) constellation: Constellation?,
        @RequestParam(required = false, defaultValue = "-99.0") magnitudeMin: Double,
        @RequestParam(required = false, defaultValue = "99.0") magnitudeMax: Double,
        @RequestParam(required = false) type: SkyObjectType?,
    ): List<DeepSkyObjectEntity> {
        return atlasService.searchDSO(
            text, rightAscension.hours, declination.deg, radius.deg,
            constellation, magnitudeMin, magnitudeMax, type,
        )
    }

    @GetMapping("satellites/{id}/position")
    fun positionOfSatellite(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        val tle = satelliteRepository.findById(id)!!.tle
        return atlasService.positionOfSatellite(locationRepository.withId(location)!!, tle, (date + time).noSeconds())
    }

    @GetMapping("satellites/{id}/altitude-points")
    fun altitudePointsOfSatellite(
        @PathVariable id: Long,
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        val tle = satelliteRepository.findById(id)!!.tle
        return atlasService
            .altitudePointsOfSatellite(locationRepository.withId(location)!!, tle, date.orNow(), stepSize)
    }

    @GetMapping("satellites")
    fun searchSatellites(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(name = "group", required = false) groups: List<SatelliteGroupType>,
    ): List<SatelliteResponse> {
        return atlasService.searchSatellites(text, groups)
    }

    @GetMapping("twilight")
    fun twilight(
        @RequestParam @Valid @Positive location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
    ): TwilightResponse {
        return atlasService.twilight(locationRepository.withId(location)!!, date.orNow())
    }
}
