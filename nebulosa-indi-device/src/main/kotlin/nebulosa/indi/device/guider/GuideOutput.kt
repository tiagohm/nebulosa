package nebulosa.indi.device.guider

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType
import java.time.Duration

interface GuideOutput : Device {

    override val type
        get() = DeviceType.GUIDE_OUTPUT

    val canPulseGuide: Boolean

    val pulseGuiding: Boolean

    fun guideNorth(duration: Duration)

    fun guideSouth(duration: Duration)

    fun guideEast(duration: Duration)

    fun guideWest(duration: Duration)
}
