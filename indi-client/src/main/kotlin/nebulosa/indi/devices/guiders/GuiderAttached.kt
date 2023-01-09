package nebulosa.indi.devices.guiders

data class GuiderAttached(override val device: Guider) : GuiderEvent<Guider>
