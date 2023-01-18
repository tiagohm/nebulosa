package nebulosa.alpaca.client

import com.fasterxml.jackson.annotation.JsonProperty

data class AlpacaResponse<out T>(
    @field:JsonProperty("ClientTransactionID") val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") val errorMessage: String = "",
    @field:JsonProperty("Value") val value: T? = null,
)
