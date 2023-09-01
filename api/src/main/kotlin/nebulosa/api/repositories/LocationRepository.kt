package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.LocationEntity
import nebulosa.api.data.entities.LocationEntity_
import org.springframework.stereotype.Component

@Component
class LocationRepository(boxStore: BoxStore) : BoxRepository<LocationEntity>() {

    override val box = boxStore.boxFor(LocationEntity::class.java)!!

    fun withName(name: String): LocationEntity? {
        return box.query()
            .equal(LocationEntity_.name, name, CASE_INSENSITIVE)
            .build()
            .use { it.findFirst() }
    }

    @PostConstruct
    private fun initialize() {
        if (isEmpty()) {
            save(LocationEntity(0, "Saint Helena", -15.9755300, -5.6987929, 819.0))
        }
    }
}
