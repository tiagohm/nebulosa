package nebulosa.api.data.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class AppPreferenceEntity(
    @Id var id: Long = 0L,
    @Unique var key: String = "",
    var value: String = "",
)
