package nebulosa.api.atlas

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.entities.BoxEntity

@Entity
data class SimbadIdentifierEntity(
    @Id override var id: Long = 0L,
    var simbadId: Long = 0L,
    @Index var name: String = "",
) : BoxEntity
