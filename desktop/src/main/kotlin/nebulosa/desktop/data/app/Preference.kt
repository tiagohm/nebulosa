package nebulosa.desktop.data.app

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "preferences")
class Preference {

    @Id
    @Column(name = "key", unique = true, columnDefinition = "TEXT")
    var key = ""

    @Column(name = "value", columnDefinition = "TEXT")
    var value = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preference) return false

        if (key != other.key) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
