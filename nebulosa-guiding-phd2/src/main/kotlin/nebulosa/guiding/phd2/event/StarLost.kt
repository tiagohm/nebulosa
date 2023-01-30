package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class StarLost(
    @field:JsonProperty("Frame") var frame: Int = 0,
    @field:JsonProperty("Time") var time: Double = 0.0,
    @field:JsonProperty("StarMass") var starMass: Double = 0.0,
    @field:JsonProperty("SNR") var snr: Double = 0.0,
    @field:JsonProperty("AvgDist") var averageDistance: Double = 0.0,
    @field:JsonProperty("ErrorCode") var errorCode: Int = 0,
    @field:JsonProperty("Status") var status: String = "",
) : PHD2Event
