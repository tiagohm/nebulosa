package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImage
import nebulosa.api.data.entities.SavedCameraImage_
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Component
class SavedCameraImageRepository(boxStore: BoxStore) : BoxRepository<SavedCameraImage>() {

    override val box = boxStore.boxFor(SavedCameraImage::class.java)!!

    fun withName(name: String): List<SavedCameraImage> {
        return box.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.find() }
    }

    fun withNameLatest(name: String): SavedCameraImage? {
        return box.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .orderDesc(SavedCameraImage_.savedAt)
            .build().use { it.findFirst() }
    }

    fun withNameAndPath(name: String, path: String): SavedCameraImage? {
        return box.query()
            .equal(SavedCameraImage_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .and()
            .equal(SavedCameraImage_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    fun withPath(path: String): SavedCameraImage? {
        return box.query()
            .equal(SavedCameraImage_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    @PostConstruct
    fun removeIfNotExists() {
        for (savedImage in box.all) {
            val path = Path.of(savedImage.path)

            if (!path.exists() || path.isDirectory()) {
                box.remove(savedImage)
            }
        }
    }
}
