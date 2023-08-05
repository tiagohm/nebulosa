package nebulosa.api.data.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class SavedCameraImageEntity(
    @Id var id: Long = 0,
    @Index var camera: String = "",
    @Index var path: String = "",
    var width: Int = 0,
    var height: Int = 0,
    var mono: Boolean = false,
    var exposure: Long = 0L,
    var savedAt: Long = 0,
)
