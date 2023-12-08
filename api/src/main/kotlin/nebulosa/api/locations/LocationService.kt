package nebulosa.api.locations

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LocationService(
    private val locationRepository: LocationRepository,
) {

    fun locations(): List<LocationEntity> {
        return locationRepository.findAll()
    }

    fun location(id: Long): LocationEntity {
        return locationRepository.findById(id).get()
    }

    fun selected(): LocationEntity? {
        return locationRepository.findFirstBySelectedTrueOrderById()
    }

    @Synchronized
    fun save(location: LocationEntity): LocationEntity {
        location.id = if (location.id <= 0L) System.currentTimeMillis() else location.id
        if (location.selected) locationRepository.unselectedAll()
        return locationRepository.save(location)
    }

    @Synchronized
    @Transactional
    fun delete(id: Long) {
        if (id > 0L && locationRepository.count() > 1) {
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
