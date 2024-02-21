package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("ArrayInDataClass", "UNCHECKED_CAST")
data class ArrayResponse<T>(
    @field:JsonProperty("ClientTransactionID") override val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") override val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") override val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") override val errorMessage: String = "",
    @field:JsonProperty("Value") override val value: Array<T> = EMPTY_ARRAY as Array<T>,
) : AlpacaResponse<Array<T>> {

    companion object {

        @JvmStatic internal val EMPTY_ARRAY = arrayOfNulls<Any?>(0)
    }
}
