package nebulosa.api.image

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import nebulosa.api.database.BoxEntity

@Entity
data class SavedImageEntity(
    @Id override var id: Long = 0L,
    var path: String = "",
    var thumbnailPath: String = "",
    var camera: String = "",
    var filter: String = "",
    var savedAt: Long = 0L,
    var width: Int = 0,
    var height: Int = 0,
    var exposureTime: Long = 0L,
    var mean: Double = 0.0,
    var hfd: Double = 0.0,
) : BoxEntity
