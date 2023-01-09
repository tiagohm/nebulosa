package nebulosa.indi.devices.guiders

import nebulosa.indi.devices.Device

interface Guider : Device {

    val canPulseGuide: Boolean

    val isPulseGuiding: Boolean

    fun guideNorth(duration: Int)

    fun guideSouth(duration: Int)

    fun guideEast(duration: Int)

    fun guideWest(duration: Int)
}
