package nebulosa.indi.device.guide

import nebulosa.indi.device.Device
import java.time.Duration

interface GuideOutput : Device {

    val canPulseGuide: Boolean

    val pulseGuiding: Boolean

    fun guideNorth(duration: Duration)

    fun guideSouth(duration: Duration)

    fun guideEast(duration: Duration)

    fun guideWest(duration: Duration)
}
