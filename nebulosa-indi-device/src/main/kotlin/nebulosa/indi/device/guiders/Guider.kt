package nebulosa.indi.device.guiders

import nebulosa.indi.device.Device

interface Guider : Device {

    val canPulseGuide: Boolean

    val pulseGuiding: Boolean

    fun guideNorth(duration: Int)

    fun guideSouth(duration: Int)

    fun guideEast(duration: Int)

    fun guideWest(duration: Int)
}
