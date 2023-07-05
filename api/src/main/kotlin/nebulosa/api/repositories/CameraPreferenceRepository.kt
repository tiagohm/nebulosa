package nebulosa.api.repositories

import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.entities.CameraPreference_
import org.springframework.stereotype.Component

@Component
class CameraPreferenceRepository(private val cameraPreferenceBox: Box<CameraPreference>) {

    fun findName(name: String): CameraPreference? {
        return cameraPreferenceBox.query()
            .equal(CameraPreference_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    fun save(entity: CameraPreference) {
        cameraPreferenceBox.put(entity)
    }
}
