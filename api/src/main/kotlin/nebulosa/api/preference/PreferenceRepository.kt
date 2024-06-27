package nebulosa.api.preference

import io.objectbox.Box
import io.objectbox.kotlin.equal
import nebulosa.api.preferences.PreferenceEntity_
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PreferenceRepository(@Qualifier("preferenceBox") override val box: Box<PreferenceEntity>) : BoxRepository<PreferenceEntity>() {

    fun existsByKey(key: String): Boolean {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.findUnique() != null }
    }

    fun findByKey(key: String): PreferenceEntity? {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.findUnique() }
    }

    @Synchronized
    fun deleteByKey(key: String) {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.remove() }
    }
}
