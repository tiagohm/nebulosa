package nebulosa.api.atlas

import io.objectbox.Box
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import io.objectbox.query.QueryCondition
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SatelliteRepository(@Qualifier("satelliteBox") override val box: Box<SatelliteEntity>) : BoxRepository<SatelliteEntity>() {

    fun search(text: String? = null, groups: List<SatelliteGroupType> = emptyList()): List<SatelliteEntity> {
        val builder = box.query()
            .also { if (!text.isNullOrBlank()) it.contains(SatelliteEntity_.name, text, CASE_INSENSITIVE) }

        if (groups.isNotEmpty()) {
            var condition: QueryCondition<SatelliteEntity> = SatelliteEntity_.groups.containsElement(groups[0].name, CASE_SENSITIVE)

            for (i in 1 until groups.size) {
                condition = condition.or(SatelliteEntity_.groups.containsElement(groups[i].name, CASE_SENSITIVE))
            }

            builder.apply(condition)
        }

        return builder
            .build()
            .use { it.find() }
    }
}
