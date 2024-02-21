package nebulosa.indi.device.camera

enum class FrameType(@JvmField val description: String) {
    LIGHT("Light"),
    DARK("Dark"),
    FLAT("Flat"),
    BIAS("Bias"),
}
