package nebulosa.api.locations

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("locations")
class LocationController(
    private val locationRepository: LocationRepository,
) {

    @GetMapping
    fun location(): List<LocationEntity> {
        return locationRepository.findAll()
    }

    @PutMapping
    @Synchronized
    fun saveLocation(@RequestBody @Valid body: LocationEntity): LocationEntity {
        locationRepository.save(body)
        return body
    }

    @Synchronized
    @DeleteMapping("{id}")
    fun deleteLocation(@PathVariable id: Long) {
        if (id > 0L && locationRepository.count() > 1) {
            locationRepository.deleteById(id)
        }
    }
}
