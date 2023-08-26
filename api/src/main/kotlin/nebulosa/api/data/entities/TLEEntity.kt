package nebulosa.api.data.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class TLEEntity(
    @Id(assignable = true) var id: Long = 0L,
    @JsonIgnore @Index var source: Long = 0L,
    var name: String = "",
    var tle: String = "",
) {

    companion object {

        @JvmStatic
        fun from(source: TLESourceEntity, lines: List<String>): TLEEntity {
            val name = lines[0]
            val id = lines[1].substring(2..6).toLong()
            val tle = lines.joinToString("\n")
            return TLEEntity(id, source.id, name, tle)
        }
    }
}
