package nebulosa.api.locations

import jakarta.validation.Valid
import nebulosa.api.data.entities.LocationEntity
import nebulosa.api.repositories.LocationRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("locations")
class LocationController(
    private val locationRepository: LocationRepository,
) {

    @GetMapping
    fun location(): List<LocationEntity> {
        return locationRepository.all()
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
        if (id > 0L && locationRepository.size > 1) {
            locationRepository.delete(id)
        }
    }
}
