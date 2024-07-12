package nebulosa.api.preference

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import nebulosa.api.database.BoxEntity

@Entity
data class PreferenceEntity(
    @Id override var id: Long = 0L,
    @Unique(onConflict = ConflictStrategy.REPLACE) @JvmField var key: String = "",
    @JvmField var value: String? = null,
) : BoxEntity
