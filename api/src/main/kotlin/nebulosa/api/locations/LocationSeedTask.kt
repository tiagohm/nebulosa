package nebulosa.api.locations

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LocationSeedTask(private val locationRepository: LocationRepository) : Runnable {

    @Scheduled(fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        if (locationRepository.isEmpty()) {
            val location = LocationEntity(0, "Null Island", 0.0, 0.0, 0.0, selected = true)
            locationRepository.save(location)
        }
    }
}
