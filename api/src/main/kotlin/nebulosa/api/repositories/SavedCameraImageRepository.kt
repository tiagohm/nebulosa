package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.entities.SavedCameraImageEntity_
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Component
class SavedCameraImageRepository(boxStore: BoxStore) : BoxRepository<SavedCameraImageEntity>() {

    override val box = boxStore.boxFor(SavedCameraImageEntity::class.java)!!

    fun withName(name: String): List<SavedCameraImageEntity> {
        return box.query()
            .equal(SavedCameraImageEntity_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.find() }
    }

    fun withNameLatest(name: String): SavedCameraImageEntity? {
        return box.query()
            .equal(SavedCameraImageEntity_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .orderDesc(SavedCameraImageEntity_.savedAt)
            .build().use { it.findFirst() }
    }

    fun withNameAndPath(name: String, path: String): SavedCameraImageEntity? {
        return box.query()
            .equal(SavedCameraImageEntity_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .and()
            .equal(SavedCameraImageEntity_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().use { it.findFirst() }
    }

    fun withPath(path: String): SavedCameraImageEntity? {
        return box.query()
            .equal(SavedCameraImageEntity_.path, path, QueryBuilder.StringOrder.CASE_SENSITIVE)
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
