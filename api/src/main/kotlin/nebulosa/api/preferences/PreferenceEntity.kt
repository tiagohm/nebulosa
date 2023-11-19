package nebulosa.api.preferences

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "preferences")
data class PreferenceEntity(
    @Id @Column(name = "key", columnDefinition = "TEXT") var key: String = "",
    @Column(name = "value", columnDefinition = "TEXT") var value: String? = null,
)
