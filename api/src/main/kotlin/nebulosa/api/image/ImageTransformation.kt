package nebulosa.api.image

import nebulosa.image.algorithms.transformation.ProtectionMethod
import nebulosa.image.format.ImageChannel

data class ImageTransformation(
    @JvmField val force: Boolean = false,
    @JvmField val calibrationGroup: String? = null,
    @JvmField val debayer: Boolean = true,
    @JvmField val stretch: Stretch = Stretch.EMPTY,
    @JvmField val mirrorHorizontal: Boolean = false,
    @JvmField val mirrorVertical: Boolean = false,
    @JvmField val invert: Boolean = false,
    @JvmField val scnr: SCNR = SCNR.EMPTY,
    @JvmField val useJPEG: Boolean = false,
) {

    data class SCNR(
        @JvmField val channel: ImageChannel? = ImageChannel.GREEN,
        @JvmField val amount: Float = 0.5f,
        @JvmField val method: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
    ) {

        companion object {

            val EMPTY = SCNR()
        }
    }

    data class Stretch(
        @JvmField val auto: Boolean = false,
        @JvmField val shadow: Int = 0,
        @JvmField val highlight: Int = 65536,
        @JvmField val midtone: Int = 32768,
        @JvmField val meanBackground: Float = 0.5f,
    ) {

        companion object {

            val EMPTY = Stretch()
        }
    }

    companion object {

        val EMPTY = ImageTransformation()
    }
}
