package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("ArrayInDataClass")
data class IntArrayResponse(
    @field:JsonProperty("ClientTransactionID") override val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") override val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") override val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") override val errorMessage: String = "",
    @field:JsonProperty("Value") override val value: IntArray = EMPTY_ARRAY,
) : AlpacaResponse<IntArray> {

    companion object {

        private val EMPTY_ARRAY = IntArray(0)
    }
}
