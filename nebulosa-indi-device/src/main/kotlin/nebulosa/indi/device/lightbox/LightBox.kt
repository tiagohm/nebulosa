package nebulosa.indi.device.lightbox

import nebulosa.indi.device.Device

interface LightBox : Device {

    val enabled: Boolean

    val intensity: Double
}
