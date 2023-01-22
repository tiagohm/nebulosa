package nebulosa.indi.device.guiders

data class GuiderAttached(override val device: Guider) : GuiderEvent<Guider>
