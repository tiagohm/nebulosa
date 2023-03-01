package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class GuideStep(
    @field:JsonProperty("Frame") var frame: Int = 0,
    @field:JsonProperty("Time") var time: Double = 0.0,
    @field:JsonProperty("Mount") var mount: String = "",
    @field:JsonProperty("dx") var dx: Double = 0.0,
    @field:JsonProperty("dy") var dy: Double = 0.0,
    @field:JsonProperty("RADistanceRaw") var raDistanceRaw: Double = 0.0,
    @field:JsonProperty("DECDistanceRaw") var decDistanceRaw: Double = 0.0,
    @field:JsonProperty("RADistanceGuide") var raDistanceGuide: Double = 0.0,
    @field:JsonProperty("DECDistanceGuide") var decDistanceGuide: Double = 0.0,
    @field:JsonProperty("RADuration") var raDuration: Long = 0L,
    @field:JsonProperty("RADirection") var raDirection: String = "",
    @field:JsonProperty("DECDuration") var decDuration: Long = 0L,
    @field:JsonProperty("DECDirection") var decDirection: String = "",
    @field:JsonProperty("StarMass") var starMass: Double = 0.0,
    @field:JsonProperty("SNR") var snr: Double = 0.0,
    @field:JsonProperty("HFD") var hfd: Double = 0.0,
    @field:JsonProperty("AvgDist") var averageDistance: Double = 0.0,
    @field:JsonProperty("RALimited") var raLimited: Boolean = false,
    @field:JsonProperty("DecLimited") var decLimited: Boolean = false,
    @field:JsonProperty("ErrorCode") var errorCode: Int = 0,
) : PHD2Event
