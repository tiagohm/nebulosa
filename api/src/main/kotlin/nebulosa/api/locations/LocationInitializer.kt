package nebulosa.api.locations

import nebulosa.api.beans.annotations.ThreadedTask
import org.springframework.stereotype.Component

@Component
@ThreadedTask
class LocationInitializer(private val locationRepository: LocationRepository) : Runnable {

    override fun run() {
        if (locationRepository.count() <= 0) {
            val location = LocationEntity(1, "Null Island", 0.0, 0.0, 0.0, selected = true)
            locationRepository.saveAndFlush(location)
        }
    }
}
