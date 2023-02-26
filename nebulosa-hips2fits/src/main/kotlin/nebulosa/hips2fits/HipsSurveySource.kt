package nebulosa.hips2fits

import com.fasterxml.jackson.annotation.JsonProperty

data class HipsSurveySource(
    @field:JsonProperty("ID") val id: String = "",
)
