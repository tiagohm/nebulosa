package nebulosa.indi.device.guider

import nebulosa.indi.device.Device

interface Guider : Device {

    val canPulseGuide: Boolean

    val pulseGuiding: Boolean

    fun guideNorth(duration: Int)

    fun guideSouth(duration: Int)

    fun guideEast(duration: Int)

    fun guideWest(duration: Int)
}
