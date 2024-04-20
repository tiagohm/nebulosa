package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class DateTimeResponse(
    @field:JsonProperty("ClientTransactionID") override val clientTransactionID: Int = 0,
    @field:JsonProperty("ServerTransactionID") override val serverTransactionID: Int = 0,
    @field:JsonProperty("ErrorNumber") override val errorNumber: Int = 0,
    @field:JsonProperty("ErrorMessage") override val errorMessage: String = "",
    @field:JsonProperty("Value") @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]")
    override val value: LocalDateTime = LocalDateTime.now(),
) : AlpacaResponse<LocalDateTime>
