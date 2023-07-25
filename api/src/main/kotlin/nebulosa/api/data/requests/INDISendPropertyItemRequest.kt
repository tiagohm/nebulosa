package nebulosa.api.data.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class INDISendPropertyItemRequest(
    @field:NotBlank val name: String = "",
    @field:NotNull val value: Any = "",
)
