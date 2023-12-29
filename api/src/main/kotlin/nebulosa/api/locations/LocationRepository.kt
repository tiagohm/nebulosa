package nebulosa.api.locations

import io.objectbox.Box
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class LocationRepository(@Qualifier("locationBox") override val box: Box<LocationEntity>) : BoxRepository<LocationEntity>() {

    fun findFirstByOrderById(): LocationEntity? {
        return box.query()
            .order(LocationEntity_.id)
            .build()
            .use { it.findFirst() }
    }

    fun findFirstBySelectedTrueOrderById(): LocationEntity? {
        return box.query()
            .equal(LocationEntity_.selected, true)
            .order(LocationEntity_.id)
            .build()
            .use { it.findFirst() }
    }

    @Synchronized
    fun unselectedAll() {
        box.put(onEach { it.selected = false })
    }
}
