package nebulosa.api.atlas

import io.objectbox.Box
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SatelliteRepository(@Qualifier("satelliteBox") override val box: Box<SatelliteEntity>) : BoxRepository<SatelliteEntity>() {

    fun search(text: String? = null, groups: Iterable<SatelliteGroupType> = emptyList(), id: Long = 0L): List<SatelliteEntity> {
        val condition = and(
            if (id > 0L) SatelliteEntity_.id.equal(id) else null,
            if (text.isNullOrBlank()) null else SatelliteEntity_.name.containsInsensitive(text),
            or(groups.map { SatelliteEntity_.groups.containsElement(it.name, CASE_SENSITIVE) }),
        )

        return (condition?.let(box::query) ?: box.query()).build().use { it.findLazy() }
    }
}
