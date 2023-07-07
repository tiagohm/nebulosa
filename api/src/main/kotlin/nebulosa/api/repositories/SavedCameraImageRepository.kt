package nebulosa.api.repositories

import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImage
import nebulosa.api.data.entities.SavedCameraImage_
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Component
class SavedCameraImageRepository(private val savedCameraImageBox: Box<SavedCameraImage>) {

    fun withId(id: Long): SavedCameraImage? {
        return savedCameraImageBox.get(id)
    }

    fun withName(name: String): List<SavedCameraImage> {
        return savedCameraImageBox.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.find() }
    }

    fun withNameLatest(name: String): SavedCameraImage? {
        return savedCameraImageBox.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .orderDesc(SavedCameraImage_.savedAt)
            .build().use { it.findFirst() }
    }

    fun withNameAndPath(name: String, path: String): SavedCameraImage? {
        return savedCameraImageBox.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .and()
            .equal(SavedCameraImage_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    fun withPath(path: String): SavedCameraImage? {
        return savedCameraImageBox.query()
            .equal(SavedCameraImage_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    fun save(entity: SavedCameraImage) {
        savedCameraImageBox.put(entity)
    }

    @PostConstruct
    fun removeIfNotExists() {
        for (savedImage in savedCameraImageBox.all) {
            val path = Path.of(savedImage.path)

            if (!path.exists() || path.isDirectory()) {
                savedCameraImageBox.remove(savedImage)
            }
        }
    }
}
