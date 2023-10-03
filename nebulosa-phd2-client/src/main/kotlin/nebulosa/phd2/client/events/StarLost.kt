package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class StarLost(
    @field:JsonProperty("Frame") val frame: Int = 0,
    @field:JsonProperty("Time") val time: Double = 0.0,
    @field:JsonProperty("StarMass") val starMass: Double = 0.0,
    @field:JsonProperty("SNR") val snr: Double = 0.0,
    @field:JsonProperty("AvgDist") val averageDistance: Double = 0.0,
    @field:JsonProperty("ErrorCode") val errorCode: Int = 0,
    @field:JsonProperty("Status") val status: String = "",
) : PHD2Event
