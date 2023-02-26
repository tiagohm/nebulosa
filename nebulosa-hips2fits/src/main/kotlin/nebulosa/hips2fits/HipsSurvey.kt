package nebulosa.hips2fits

import com.fasterxml.jackson.annotation.JsonProperty

data class HipsSurvey(
    @field:JsonProperty("ID") val id: String = "",
)
