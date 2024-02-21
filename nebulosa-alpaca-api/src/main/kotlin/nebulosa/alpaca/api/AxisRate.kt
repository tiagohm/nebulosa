package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

data class AxisRate(
    @field:JsonProperty("Maximum") val maximum: Double,
    @field:JsonProperty("Minimum") val minimum: Double,
)
