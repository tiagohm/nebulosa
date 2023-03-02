package nebulosa.horizons

import com.fasterxml.jackson.annotation.JsonProperty

data class SpkFile(
    @field:JsonProperty("spk_file_id") val id: Int = 0,
    @field:JsonProperty("spk") val spk: String = "",
    @field:JsonProperty("error") val error: String = "",
)
