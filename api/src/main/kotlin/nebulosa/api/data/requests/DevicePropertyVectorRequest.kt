package nebulosa.api.data.requests

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import nebulosa.api.data.enums.DevicePropertyVectorType

data class DevicePropertyVectorRequest(
    @field:NotBlank val name: String,
    @field:NotNull val type: DevicePropertyVectorType,
    @field:NotEmpty @field:Valid val properties: List<DevicePropertyRequest>,
)
