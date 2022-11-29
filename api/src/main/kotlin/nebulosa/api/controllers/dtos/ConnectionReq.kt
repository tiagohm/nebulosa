package nebulosa.api.controllers.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ConnectionReq(
    @field:NotBlank val host: String,
    @field:Positive val port: Int,
)
