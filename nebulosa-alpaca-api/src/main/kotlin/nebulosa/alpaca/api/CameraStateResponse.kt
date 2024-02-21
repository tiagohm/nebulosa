package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

data class CameraStateResponse(
    @field:JsonProperty("ClientTransactionID") override val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") override val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") override val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") override val errorMessage: String = "",
    @field:JsonProperty("Value") override val value: CameraState = CameraState.IDLE,
) : AlpacaResponse<CameraState>
