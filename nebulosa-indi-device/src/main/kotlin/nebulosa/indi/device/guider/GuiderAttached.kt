package nebulosa.indi.device.guider

data class GuiderAttached(override val device: Guider) : GuiderEvent<Guider>
