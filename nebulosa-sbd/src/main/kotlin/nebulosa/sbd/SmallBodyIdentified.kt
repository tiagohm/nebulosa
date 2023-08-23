package nebulosa.sbd

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class SmallBodyIdentified(
    @JsonAlias("n_first_pass", "n_second_pass") @JsonProperty("count") val count: Int,
    @JsonAlias("fields_first", "fields_second") @JsonProperty("fields") val fields: List<String>,
    @JsonAlias("data_first_pass", "data_second_pass") @JsonProperty("data") val data: List<List<String>>,
)
