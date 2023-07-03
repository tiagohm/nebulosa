package nebulosa.api.data.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Preference(
    @Id var id: Long = 0,
    @Index var key: String = "",
    var value: String = "",
)
