package nebulosa.api.cameras

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class CameraCaptureHistory(
    @Id var id: Long = 0L,
    var name: String = "",
    var path: String = "",
    var savedAt: Long = 0,
)
