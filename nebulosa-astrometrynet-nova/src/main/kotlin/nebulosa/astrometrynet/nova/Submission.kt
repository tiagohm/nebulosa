package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonProperty

data class Submission(
    @field:JsonProperty("status") val status: String = "",
    @field:JsonProperty("subid") val subId: Int = 0,
)
