package nebulosa.api.locations

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("locations")
class LocationController(
    private val locationService: LocationService,
) {

    @GetMapping
    fun locations(): List<LocationEntity> {
        return locationService.locations()
    }

    @GetMapping("{id}")
    fun location(@PathVariable id: Long): LocationEntity {
        return locationService.location(id)
    }

    @GetMapping("selected")
    fun selected(): LocationEntity? {
        return locationService.selected()
    }

    @PutMapping
    fun saveLocation(@RequestBody @Valid body: LocationEntity): LocationEntity {
        return locationService.save(body)
    }

    @DeleteMapping("{id}")
    fun deleteLocation(@PathVariable id: Long) {
        locationService.delete(id)
    }
}
