package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel

/**
 * The Subtractive Chromatic Noise Reduction (SCNR) technique
 * has been implemented mainly to deal with green noisy pixels.
 *
 * We'll be removing some [channel] pixels, but, how much should
 * be removed from the image? Obviously, we don't want to destroy true channel data.
 * For stellar objects, for example, we must make sure no green will be removed.
 * A [protectionMethod] should be used to avoid destroying correct channel data.
 *
 * Mask-protected SCNR is an aggressive and efficient
 * technique to remove green pixels.
 *
 * Its main drawback is that it can introduce a magenta cast
 * to the sky background, which must be controlled by a careful
 * dosage of the [amount] parameter.
 *
 * @See <a href="https://www.pixinsight.com/doc/legacy/LE/21_noise_reduction/scnr/scnr.html">PixInsight</a>
 */
class SubtractiveChromaticNoiseReduction(
    val channel: ImageChannel,
    val amount: Float,
    val protectionMethod: ProtectionMethod,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (source.mono || channel == ImageChannel.GRAY) return source

        val p0 = ((channel.offset - 1) % 3).let { if (it < 0) it + 3 else it }
        val p1 = (channel.offset + 1) % 3

        for (i in source.data.indices step 3) {
            source.data[i + channel.offset] = protectionMethod
                .compute(source.data[i + p0], source.data[i + p1], source.data[i + channel.offset], amount)
        }

        return source
    }
}
