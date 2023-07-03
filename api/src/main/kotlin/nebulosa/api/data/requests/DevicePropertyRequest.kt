package nebulosa.api.data.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DevicePropertyRequest(
    @field:NotBlank val name: String,
    @field:NotNull val value: Any,
)
