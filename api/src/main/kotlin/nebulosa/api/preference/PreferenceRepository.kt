package nebulosa.api.preference

import io.objectbox.Box
import io.objectbox.kotlin.equal
import nebulosa.api.repositories.BoxRepository

class PreferenceRepository(override val box: Box<PreferenceEntity>) : BoxRepository<PreferenceEntity>() {

    fun existsByKey(key: String): Boolean {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.findUnique() != null }
    }

    fun findByKey(key: String): PreferenceEntity? {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.findUnique() }
    }

    fun deleteByKey(key: String) {
        return box.query(PreferenceEntity_.key equal key)
            .build().use { it.remove() }
    }
}
