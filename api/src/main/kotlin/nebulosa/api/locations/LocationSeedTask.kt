package nebulosa.api.locations

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LocationSeedTask(private val locationRepository: LocationRepository) : Runnable {

    @Scheduled(initialDelay = 1L, fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        if (locationRepository.count() <= 0) {
            val location = LocationEntity(1, "Null Island", 0.0, 0.0, 0.0, selected = true)
            locationRepository.saveAndFlush(location)
        }
    }
}
