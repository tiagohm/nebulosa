package nebulosa.api.image

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.range
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
    @JvmField val adjustment: Adjustment = Adjustment.EMPTY,
) : Validatable {

    override fun validate() {
        stretch.validate()
        scnr.validate()
        adjustment.validate()
    }

    data class SCNR(
        @JvmField val channel: ImageChannel? = ImageChannel.GREEN,
        @JvmField val amount: Float = 0.5f,
        @JvmField val method: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
    ) : Validatable {

        override fun validate() {
            amount.range(0f, 1f)
        }

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
    ) : Validatable {

        override fun validate() {
            if (!auto) {
                shadow.range(0, 65536)
                highlight.range(0, 65536)
                midtone.range(0, 65536)
                meanBackground.range(0f, 1f)
            }
        }

        companion object {

            val EMPTY = Stretch()
        }
    }

    companion object {

        val EMPTY = ImageTransformation()
    }

    data class Adjustment(
        @JvmField val enabled: Boolean = false,
        @JvmField val contrast: Level = Level.DISABLED,
        @JvmField val brightness: Level = Level.DISABLED,
        @JvmField val exposure: Level = Level.DISABLED,
        @JvmField val gamma: Level = Level.DISABLED,
        @JvmField val saturation: Level = Level.DISABLED,
        @JvmField val fade: Level = Level.DISABLED,
    ) : Validatable {

        val canTransform
            get() = enabled && (contrast.canTransform || brightness.canTransform || saturation.canTransform || exposure.canTransform || gamma.canTransform || fade.canTransform)

        override fun validate() {
            contrast.validate()
            brightness.validate()
            exposure.validate()
            gamma.validate()
            saturation.validate()
            fade.validate()
        }

        data class Level(
            @JvmField val enabled: Boolean = false,
            @JvmField val value: Float = 0f,
        ) : Validatable {

            val canTransform
                get() = enabled && value != 0f

            override fun validate() {
                if (enabled) value.range(0f, 1f)
            }

            companion object {

                val DISABLED = Level()
            }
        }

        companion object {

            val EMPTY = Adjustment()
        }
    }

}
