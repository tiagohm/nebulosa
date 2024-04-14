package nebulosa.api.image

import nebulosa.image.algorithms.transformation.ProtectionMethod
import nebulosa.image.format.ImageChannel

data class ImageTransformation(
    val force: Boolean = false,
    val calibrationGroup: String? = null,
    val debayer: Boolean = true,
    val stretch: Stretch = Stretch.EMPTY,
    val mirrorHorizontal: Boolean = false,
    val mirrorVertical: Boolean = false,
    val invert: Boolean = false,
    val scnr: SCNR = SCNR.EMPTY,
) {

    data class SCNR(
        val channel: ImageChannel? = ImageChannel.GREEN,
        val amount: Float = 0.5f,
        val method: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
    ) {

        companion object {

            @JvmStatic val EMPTY = SCNR()
        }
    }

    data class Stretch(
        val auto: Boolean = false,
        val shadow: Float = 0f,
        val highlight: Float = 0.5f,
        val midtone: Float = 1f,
    ) {

        companion object {

            @JvmStatic val EMPTY = Stretch()
        }
    }

    companion object {

        @JvmStatic val EMPTY = ImageTransformation()
    }
}
