package nebulosa.api.locations

import nebulosa.api.beans.annotations.ThreadedTask
import org.springframework.stereotype.Component

@Component
@ThreadedTask
class LocationInitializerTask(private val locationRepository: LocationRepository) : Runnable {

    override fun run() {
        if (locationRepository.count() <= 0) {
            val location = LocationEntity(System.currentTimeMillis(), "Saint Helena", -15.9755300, -5.6987929, 819.0)
            locationRepository.saveAndFlush(location)
        }
    }
}
