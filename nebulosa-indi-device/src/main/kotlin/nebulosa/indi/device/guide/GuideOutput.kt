package nebulosa.indi.device.guide

import nebulosa.indi.device.Device

interface GuideOutput : Device {

    val canPulseGuide: Boolean

    val pulseGuiding: Boolean

    fun guideNorth(duration: Int)

    fun guideSouth(duration: Int)

    fun guideEast(duration: Int)

    fun guideWest(duration: Int)
}
