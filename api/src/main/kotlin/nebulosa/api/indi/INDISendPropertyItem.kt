package nebulosa.api.indi

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class INDISendPropertyItem(
    @field:NotBlank val name: String = "",
    @field:NotNull val value: Any = "",
)
