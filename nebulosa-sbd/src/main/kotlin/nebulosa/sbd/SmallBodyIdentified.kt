package nebulosa.sbd

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class SmallBodyIdentified(
    @field:JsonAlias("n_first_pass", "n_second_pass") @field:JsonProperty("count") val count: Int = 0,
    @field:JsonAlias("fields_first", "fields_second") @field:JsonProperty("fields") val fields: List<String> = emptyList(),
    @field:JsonAlias("data_first_pass", "data_second_pass") @field:JsonProperty("data") val data: List<List<String>> = emptyList(),
)
