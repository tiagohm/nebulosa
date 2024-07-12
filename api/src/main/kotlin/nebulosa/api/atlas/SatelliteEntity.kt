package nebulosa.api.atlas

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import nebulosa.api.database.BoxEntity

@Entity
data class SatelliteEntity(
    @Id(assignable = true) override var id: Long = 0L,
    @JvmField var name: String = "",
    @JvmField var tle: String = "",
    @JvmField var groups: MutableList<String> = ArrayList(0),
) : BoxEntity
