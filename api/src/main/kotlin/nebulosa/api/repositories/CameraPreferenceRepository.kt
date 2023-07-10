package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.CameraPreferenceEntity
import nebulosa.api.data.entities.CameraPreferenceEntity_
import org.springframework.stereotype.Component

@Component
class CameraPreferenceRepository(boxStore: BoxStore) : BoxRepository<CameraPreferenceEntity>() {

    override val box = boxStore.boxFor(CameraPreferenceEntity::class.java)!!

    fun withName(name: String): CameraPreferenceEntity? {
        return box.query()
            .equal(CameraPreferenceEntity_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }
}
