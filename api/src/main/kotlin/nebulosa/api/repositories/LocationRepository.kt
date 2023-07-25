package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.LocationEntity
import nebulosa.api.data.entities.LocationEntity_
import org.springframework.stereotype.Component

@Component
class LocationRepository(boxStore: BoxStore) : BoxRepository<LocationEntity>() {

    override val box = boxStore.boxFor(LocationEntity::class.java)!!

    fun withName(name: String): LocationEntity? {
        return box.query()
            .equal(LocationEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build().use { it.findFirst() }
    }
}
