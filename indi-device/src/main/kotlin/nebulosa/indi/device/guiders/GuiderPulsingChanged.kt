package nebulosa.indi.device.guiders

data class GuiderPulsingChanged(override val device: Guider) : GuiderEvent<Guider>
