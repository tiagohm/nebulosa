package nebulosa.api.configs

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "configs")
data class ConfigEntity(
    @Id @Column(name = "key", columnDefinition = "TEXT") var key: String = "",
    @Column(name = "value", columnDefinition = "TEXT") var value: String? = null,
)
