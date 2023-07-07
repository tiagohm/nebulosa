package nebulosa.api.data.requests

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import nebulosa.api.data.enums.INDIPropertyType

data class INDISendPropertyRequest(
    @field:NotBlank val name: String,
    @field:NotNull val type: INDIPropertyType,
    @field:NotEmpty @field:Valid val properties: List<INDIPropertyRequest>,
)
