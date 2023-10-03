package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class GuideStep(
    @field:JsonProperty("Frame") val frame: Int = 0,
    @field:JsonProperty("Time") val time: Double = 0.0,
    @field:JsonProperty("Mount") val mount: String = "",
    @field:JsonProperty("dx") val dx: Double = 0.0,
    @field:JsonProperty("dy") val dy: Double = 0.0,
    @field:JsonProperty("RADistanceRaw") val raDistanceRaw: Double = 0.0,
    @field:JsonProperty("DECDistanceRaw") val decDistanceRaw: Double = 0.0,
    @field:JsonProperty("RADistanceGuide") val raDistanceGuide: Double = 0.0,
    @field:JsonProperty("DECDistanceGuide") val decDistanceGuide: Double = 0.0,
    @field:JsonProperty("RADuration") val raDuration: Long = 0L,
    @field:JsonProperty("RADirection") val raDirection: String = "",
    @field:JsonProperty("DECDuration") val decDuration: Long = 0L,
    @field:JsonProperty("DECDirection") val decDirection: String = "",
    @field:JsonProperty("StarMass") val starMass: Double = 0.0,
    @field:JsonProperty("SNR") val snr: Double = 0.0,
    @field:JsonProperty("HFD") val hfd: Double = 0.0,
    @field:JsonProperty("AvgDist") val averageDistance: Double = 0.0,
    @field:JsonProperty("RALimited") val raLimited: Boolean = false,
    @field:JsonProperty("DecLimited") val decLimited: Boolean = false,
    @field:JsonProperty("ErrorCode") val errorCode: Int = 0,
) : PHD2Event
