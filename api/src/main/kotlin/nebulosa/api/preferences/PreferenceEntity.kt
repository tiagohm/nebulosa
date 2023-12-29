package nebulosa.api.preferences

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import nebulosa.api.entities.BoxEntity

@Entity
data class PreferenceEntity(
    @Id override var id: Long = 0L,
    @Unique(onConflict = ConflictStrategy.REPLACE) var key: String = "",
    var value: String? = null,
) : BoxEntity
