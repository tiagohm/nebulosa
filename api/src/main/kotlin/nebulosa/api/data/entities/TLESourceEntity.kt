package nebulosa.api.data.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class TLESourceEntity(
    @Id var id: Long = 0L,
    @Index var url: String = "",
    var updatedAt: Long = 0,
    var enabled: Boolean = true,
    var deletable: Boolean = false,
)
