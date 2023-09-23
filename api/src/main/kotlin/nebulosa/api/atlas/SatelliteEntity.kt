package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "satellites")
data class SatelliteEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @Column(name = "name", columnDefinition = "TEXT") var name: String = "",
    @Column(name = "tle", columnDefinition = "TEXT") var tle: String = "",
    @JsonIgnore @Column(name = "group_type", columnDefinition = "INT8") var groupType: Long = 0L,
) : Persistable<Long> {

    @get:JsonGetter val groups
        get() = SatelliteGroupType.entries.filter { groupType and (1L shl it.ordinal) != 0L }

    override fun getId() = id

    @JsonIgnore
    override fun isNew() = true
}
