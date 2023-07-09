package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.entities.CameraPreference_
import org.springframework.stereotype.Component

@Component
class CameraPreferenceRepository(boxStore: BoxStore) : BoxRepository<CameraPreference>() {

    override val box = boxStore.boxFor(CameraPreference::class.java)!!

    fun withName(name: String): CameraPreference? {
        return box.query()
            .equal(CameraPreference_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }
}
