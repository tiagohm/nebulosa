package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonIgnore
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import nebulosa.api.entities.BoxEntity

@Entity
data class SatelliteEntity(
    @Id(assignable = true) override var id: Long = 0L,
    var name: String = "",
    var tle: String = "",
    @JsonIgnore var groups: MutableList<String> = ArrayList(0),
) : BoxEntity
