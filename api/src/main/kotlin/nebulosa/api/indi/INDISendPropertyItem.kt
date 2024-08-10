package nebulosa.api.indi

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class INDISendPropertyItem(
    @field:NotBlank @JvmField val name: String = "",
    @field:NotNull @JvmField val value: Any = "",
)
