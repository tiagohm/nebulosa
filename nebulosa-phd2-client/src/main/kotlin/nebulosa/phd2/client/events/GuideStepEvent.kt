package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import nebulosa.guiding.GuideDirection

data class GuideStepEvent(
    @field:JsonAlias("Frame") val frame: Int = 0,
    @field:JsonAlias("Time") val time: Double = 0.0,
    @field:JsonAlias("Mount") val mount: String = "",
    @field:JsonProperty("dx") val dx: Double = 0.0,
    @field:JsonProperty("dy") val dy: Double = 0.0,
    @field:JsonAlias("RADistanceRaw") val raDistance: Double = 0.0,
    @field:JsonAlias("DECDistanceRaw") val decDistance: Double = 0.0,
    @field:JsonAlias("RADistanceGuide") val raDistanceGuide: Double = 0.0,
    @field:JsonAlias("DECDistanceGuide") val decDistanceGuide: Double = 0.0,
    @field:JsonAlias("RADuration") val raDuration: Long = 0L,
    @field:JsonAlias("RADirection") val raDirection: GuideDirection = GuideDirection.WEST,
    @field:JsonAlias("DECDuration") val decDuration: Long = 0L,
    @field:JsonAlias("DECDirection") val decDirection: GuideDirection = GuideDirection.NORTH,
    @field:JsonAlias("StarMass") val starMass: Double = 0.0,
    @field:JsonAlias("SNR") val snr: Double = 0.0,
    @field:JsonAlias("HFD") val hfd: Double = 0.0,
    @field:JsonAlias("AvgDist") val averageDistance: Double = 0.0,
    @field:JsonAlias("RALimited") val raLimited: Boolean = false,
    @field:JsonAlias("DecLimited") val decLimited: Boolean = false,
    @field:JsonAlias("ErrorCode") val errorCode: Int = 0,
) : PHD2Event
