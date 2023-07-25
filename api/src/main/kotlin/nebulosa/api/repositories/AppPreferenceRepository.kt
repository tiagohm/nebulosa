package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.AppPreferenceEntity
import nebulosa.api.data.entities.AppPreferenceEntity_
import org.springframework.stereotype.Component

@Component
class AppPreferenceRepository(boxStore: BoxStore) : BoxRepository<AppPreferenceEntity>() {

    override val box = boxStore.boxFor(AppPreferenceEntity::class.java)!!

    fun withKey(key: String): AppPreferenceEntity? {
        return box.query()
            .equal(AppPreferenceEntity_.key, key, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }
}
