package nebulosa.image.algorithms.transformation

import nebulosa.image.Image
import nebulosa.image.ImageChannel
import nebulosa.image.algorithms.TransformAlgorithm

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
data class SubtractiveChromaticNoiseReduction(
    private val channel: ImageChannel,
    private val amount: Float,
    private val protectionMethod: ProtectionMethod,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (source.mono || channel == ImageChannel.GRAY) return source

        val p0 = (channel.offset + 2) % 3
        val p1 = (channel.offset + 1) % 3

        for (i in source.red.indices) {
            source.data[channel.offset][i] = protectionMethod
                .compute(source.data[p0][i], source.data[p1][i], source.data[channel.offset][i], amount)
        }

        return source
    }
}
