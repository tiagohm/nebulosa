package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import nebulosa.guiding.GuideDirection
import nebulosa.guiding.GuideStep

data class GuideStepEvent(
    @field:JsonAlias("Frame") override val frame: Int = 0,
    @field:JsonAlias("Time") val time: Double = 0.0,
    @field:JsonAlias("Mount") val mount: String = "",
    @field:JsonProperty("dx") override val dx: Double = 0.0,
    @field:JsonProperty("dy") override val dy: Double = 0.0,
    @field:JsonAlias("RADistanceRaw") override val raDistance: Double = 0.0,
    @field:JsonAlias("DECDistanceRaw") override val decDistance: Double = 0.0,
    @field:JsonAlias("RADistanceGuide") override val raDistanceGuide: Double = 0.0,
    @field:JsonAlias("DECDistanceGuide") override val decDistanceGuide: Double = 0.0,
    @field:JsonAlias("RADuration") override val raDuration: Long = 0L,
    @field:JsonAlias("RADirection") override val raDirection: GuideDirection = GuideDirection.WEST,
    @field:JsonAlias("DECDuration") override val decDuration: Long = 0L,
    @field:JsonAlias("DECDirection") override val decDirection: GuideDirection = GuideDirection.NORTH,
    @field:JsonAlias("StarMass") override val starMass: Double = 0.0,
    @field:JsonAlias("SNR") override val snr: Double = 0.0,
    @field:JsonAlias("HFD") override val hfd: Double = 0.0,
    @field:JsonAlias("AvgDist") override val averageDistance: Double = 0.0,
    @field:JsonAlias("RALimited") val raLimited: Boolean = false,
    @field:JsonAlias("DecLimited") val decLimited: Boolean = false,
    @field:JsonAlias("ErrorCode") val errorCode: Int = 0,
) : PHD2Event, GuideStep
