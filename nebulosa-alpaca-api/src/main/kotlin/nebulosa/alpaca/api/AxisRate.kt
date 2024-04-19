package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class AxisRate(
    @field:JsonProperty("Maximum") val maximum: BigDecimal = BigDecimal.ZERO,
    @field:JsonProperty("Minimum") val minimum: BigDecimal = BigDecimal.ZERO,
)
