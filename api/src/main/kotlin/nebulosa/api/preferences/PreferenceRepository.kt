package nebulosa.api.preferences

import io.objectbox.Box
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PreferenceRepository(@Qualifier("preferenceBox") override val box: Box<PreferenceEntity>) : BoxRepository<PreferenceEntity>() {

    fun existsByKey(key: String): Boolean {
        return box.query()
            .equal(PreferenceEntity_.key, key, CASE_SENSITIVE)
            .build()
            .use { it.findUnique() != null }
    }

    fun findByKey(key: String): PreferenceEntity? {
        return box.query()
            .equal(PreferenceEntity_.key, key, CASE_SENSITIVE)
            .build()
            .use { it.findUnique() }
    }

    @Synchronized
    fun deleteByKey(key: String) {
        return box.query()
            .equal(PreferenceEntity_.key, key, CASE_SENSITIVE)
            .build()
            .use { it.remove() }
    }
}
