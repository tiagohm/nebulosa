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
import nebulosa.api.repositories.StarRepository
import nebulosa.api.utils.noSeconds
import nebulosa.api.utils.orNow
import nebulosa.api.utils.plus
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime

@RestController
class AtlasController(
    private val atlasService: AtlasService,
    private val locationRepository: LocationRepository,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
) {

    @GetMapping("imageOfSun")
    fun imageOfSun(response: HttpServletResponse) {
        atlasService.imageOfSun(response)
    }

    @GetMapping("positionOfSun")
    fun positionOfSun(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfSun(locationRepository.withId(location)!!, (date + time).noSeconds())
    }

    @GetMapping("positionOfMoon")
    fun positionOfMoon(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfMoon(locationRepository.withId(location)!!, (date + time).noSeconds())
    }

    @GetMapping("positionOfPlanet")
    fun positionOfPlanet(
        @RequestParam location: Long,
        @RequestParam @Valid @NotBlank code: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfPlanet(locationRepository.withId(location)!!, code, (date + time).noSeconds())
    }

    @GetMapping("positionOfStar")
    fun positionOfStar(
        @RequestParam location: Long,
        @RequestParam @Valid @Positive star: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfStar(locationRepository.withId(location)!!, starRepository.withId(star)!!, (date + time).noSeconds())
    }

    @GetMapping("positionOfDSO")
    fun positionOfDSO(
        @RequestParam location: Long,
        @RequestParam @Valid @Positive dso: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfDSO(locationRepository.withId(location)!!, deepSkyObjectRepository.withId(dso)!!, (date + time).noSeconds())
    }

    @GetMapping("positionOfSatellite")
    fun positionOfSatellite(
        @RequestParam location: Long,
        @RequestParam @Valid @NotBlank tle: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfSatellite(locationRepository.withId(location)!!, tle, (date + time).noSeconds())
    }

    @GetMapping("searchSatellites")
    fun searchSatellites(
        @RequestParam(required = false, defaultValue = "") text: String,
        @RequestParam(name = "group", required = false) groups: List<SatelliteGroupType>,
    ): List<SatelliteResponse> {
        return atlasService.searchSatellites(text, groups)
    }

    @GetMapping("twilight")
    fun twilight(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
    ): TwilightResponse {
        return atlasService.twilight(locationRepository.withId(location)!!, date.orNow())
    }

    @GetMapping("altitudePointsOfSun")
    fun altitudePointsOfSun(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfSun(locationRepository.withId(location)!!, date.orNow(), stepSize)
    }

    @GetMapping("altitudePointsOfMoon")
    fun altitudePointsOfMoon(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfMoon(locationRepository.withId(location)!!, date.orNow(), stepSize)
    }

    @GetMapping("altitudePointsOfPlanet")
    fun altitudePointsOfPlanet(
        @RequestParam location: Long,
        @RequestParam @Valid @NotBlank code: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfPlanet(locationRepository.withId(location)!!, code, date.orNow(), stepSize)
    }

    @GetMapping("altitudePointsOfStar")
    fun altitudePointsOfStar(
        @RequestParam location: Long,
        @RequestParam @Valid @Positive star: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService
            .altitudePointsOfStar(locationRepository.withId(location)!!, starRepository.withId(star)!!, date.orNow(), stepSize)
    }

    @GetMapping("altitudePointsOfDSO")
    fun altitudePointsOfDSO(
        @RequestParam location: Long,
        @RequestParam @Valid @Positive dso: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService
            .altitudePointsOfDSO(locationRepository.withId(location)!!, deepSkyObjectRepository.withId(dso)!!, date.orNow(), stepSize)
    }

    @GetMapping("altitudePointsOfSatellite")
    fun altitudePointsOfSatellite(
        @RequestParam location: Long,
        @RequestParam @Valid @NotBlank tle: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false, defaultValue = "1") stepSize: Int,
    ): List<DoubleArray> {
        return atlasService
            .altitudePointsOfSatellite(locationRepository.withId(location)!!, tle, date.orNow(), stepSize)
    }

    @GetMapping("searchMinorPlanet")
    fun searchMinorPlanet(@RequestParam @Valid @NotBlank text: String): MinorPlanetResponse {
        return atlasService.searchMinorPlanet(text)
    }

    @GetMapping("searchStar")
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

    @GetMapping("searchDSO")
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
}
