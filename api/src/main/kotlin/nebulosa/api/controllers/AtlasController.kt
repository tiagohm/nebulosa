package nebulosa.api.controllers

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import nebulosa.api.data.entities.Location
import nebulosa.api.data.responses.BodyPositionResponse
import nebulosa.api.data.responses.TwilightResponse
import nebulosa.api.helpers.noSeconds
import nebulosa.api.helpers.orNow
import nebulosa.api.helpers.plus
import nebulosa.api.repositories.LocationRepository
import nebulosa.api.services.AtlasService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime

@RestController
class AtlasController(
    private val atlasService: AtlasService,
    private val locationRepository: LocationRepository,
) {

    @GetMapping("locations")
    fun location(): List<Location> {
        return locationRepository.all()
    }

    @PutMapping("location")
    fun updateLocation(
        @RequestParam(required = false, defaultValue = "0") id: Long,
        @RequestBody @Valid body: Location,
    ) {
        val location = if (id > 0) locationRepository.withId(id) else null
        locationRepository.save(body.also { it.id = location?.id ?: 0L })
    }

    @DeleteMapping("location")
    fun deleteLocation(@RequestParam id: Long) {
        locationRepository.delete(id)
    }

    @GetMapping("sun")
    fun sun(response: HttpServletResponse) {
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

    @GetMapping("twilight")
    fun twilight(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
    ): TwilightResponse {
        return atlasService.twilight(locationRepository.withId(location)!!, date.orNow())
    }

    @GetMapping("positionOfMoon")
    fun positionOfMoon(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
        @DateTimeFormat(pattern = "HH:mm") @RequestParam(required = false) time: LocalTime?,
    ): BodyPositionResponse {
        return atlasService.positionOfMoon(locationRepository.withId(location)!!, (date + time).noSeconds())
    }

    @GetMapping("altitudePointsOfSun")
    fun altitudePointsOfSun(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfSun(locationRepository.withId(location)!!, date.orNow())
    }

    @GetMapping("altitudePointsOfMoon")
    fun altitudePointsOfMoon(
        @RequestParam location: Long,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) date: LocalDate?,
    ): List<DoubleArray> {
        return atlasService.altitudePointsOfMoon(locationRepository.withId(location)!!, date.orNow())
    }
}
