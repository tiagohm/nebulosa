package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

data class EquatorialCoordinateTypeResponse(
    @field:JsonProperty("ClientTransactionID") override val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") override val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") override val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") override val errorMessage: String = "",
    @field:JsonProperty("Value") override val value: EquatorialCoordinateType = EquatorialCoordinateType.J2000,
) : AlpacaResponse<EquatorialCoordinateType>
