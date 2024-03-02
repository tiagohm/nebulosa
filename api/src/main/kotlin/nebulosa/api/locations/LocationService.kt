package nebulosa.api.locations

import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationRepository: LocationRepository,
) {

    fun locations(): List<LocationEntity> {
        return locationRepository.findAll()
    }

    fun location(id: Long): LocationEntity {
        return locationRepository.find(id)!!
    }

    val selected
        get() = locationRepository.findFirstBySelectedTrueOrderById()

    @Synchronized
    fun save(location: LocationEntity): LocationEntity {
        if (location.selected) locationRepository.unselectedAll()
        locationRepository.save(location)
        return location
    }

    @Synchronized
    fun delete(id: Long) {
        if (id > 0L && locationRepository.size > 1) {
            var location = location(id)

            locationRepository.delete(location)

            if (location.selected) {
                location = locationRepository.findFirstByOrderById()!!
                location.selected = true
                save(location)
            }
        }
    }
}
