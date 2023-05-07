package nebulosa.desktop.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "preferences")
data class PreferenceEntity(
    @Id
    @Column(name = "key", unique = true, columnDefinition = "TEXT")
    var key: String = "",

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    var value: String = "",
)
