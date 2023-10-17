package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class StarLostEvent(
    @field:JsonAlias("Frame") val frame: Int = 0,
    @field:JsonAlias("Time") val time: Double = 0.0,
    @field:JsonAlias("StarMass") val starMass: Double = 0.0,
    @field:JsonAlias("SNR") val snr: Double = 0.0,
    @field:JsonAlias("AvgDist") val averageDistance: Double = 0.0,
    @field:JsonAlias("ErrorCode") val errorCode: Int = 0,
    @field:JsonAlias("Status") val status: String = "",
) : PHD2Event
