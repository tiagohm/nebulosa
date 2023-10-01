package nebulosa.api.indi

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import nebulosa.indi.protocol.PropertyType

data class INDISendProperty(
    @field:NotBlank val name: String = "",
    @field:NotNull val type: PropertyType = PropertyType.SWITCH,
    @field:NotEmpty @field:Valid val items: List<INDISendPropertyItem> = emptyList(),
)
