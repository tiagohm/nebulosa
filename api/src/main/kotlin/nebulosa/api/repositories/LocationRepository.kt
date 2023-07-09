package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.Location
import nebulosa.api.data.entities.Location_
import org.springframework.stereotype.Component

@Component
class LocationRepository(boxStore: BoxStore) : BoxRepository<Location>() {

    override val box = boxStore.boxFor(Location::class.java)!!

    fun withName(name: String): Location? {
        return box.query()
            .equal(Location_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build().use { it.findFirst() }
    }
}
