package nebulosa.indi.device.camera

import nebulosa.fits.frame
import nebulosa.image.format.ReadableHeader

enum class FrameType(@JvmField val description: String) {
    LIGHT("Light"),
    DARK("Dark"),
    FLAT("Flat"),
    BIAS("Bias");

    companion object {

        @JvmStatic val ReadableHeader.frameType
            get() = frame?.let {
                if (it.contains("LIGHT", true)) LIGHT
                else if (it.contains("DARK", true)) DARK
                else if (it.contains("FLAT", true)) FLAT
                else if (it.contains("BIAS", true)) BIAS
                else null
            }
    }
}
