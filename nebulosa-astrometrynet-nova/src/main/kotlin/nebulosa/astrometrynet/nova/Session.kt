package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonProperty

data class Session(
    @field:JsonProperty("status") val status: String = "",
    @field:JsonProperty("message") val message: String = "",
    @field:JsonProperty("session") val session: String = "",
)
