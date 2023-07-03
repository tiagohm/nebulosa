package nebulosa.api.data.dtos

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class ConnectionRequest(
    @field:NotBlank val host: String,
    @field:Range(min = 1, max = 65535) val port: Int,
)
