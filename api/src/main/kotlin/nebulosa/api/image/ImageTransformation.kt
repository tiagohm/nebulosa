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
) {

    data class SCNR(
        @JvmField val channel: ImageChannel? = ImageChannel.GREEN,
        @JvmField val amount: Float = 0.5f,
        @JvmField val method: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
    ) {

        companion object {

            @JvmStatic val EMPTY = SCNR()
        }
    }

    data class Stretch(
        @JvmField val auto: Boolean = false,
        @JvmField val shadow: Float = 0f,
        @JvmField val highlight: Float = 0.5f,
        @JvmField val midtone: Float = 1f,
    ) {

        companion object {

            @JvmStatic val EMPTY = Stretch()
        }
    }

    companion object {

        @JvmStatic val EMPTY = ImageTransformation()
    }
}
