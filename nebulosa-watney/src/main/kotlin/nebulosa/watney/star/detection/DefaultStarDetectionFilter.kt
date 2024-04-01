package nebulosa.watney.star.detection

import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.image.Image
import kotlin.math.max

object DefaultStarDetectionFilter : StarDetectionFilter {

    override fun filter(bins: MutableList<StarPixelBin>, image: Image) {
        val binIter = bins.iterator()

        while (binIter.hasNext()) {
            val bin = binIter.next()

            val remove = isTooSmall(bin) ||
                    isTooLarge(bin, image) ||
                    isStreakish(bin) ||
                    isNotRound(bin)

            if (remove) {
                binIter.remove()
            }
        }

        // TODO: Additional boxing of each star, create profile, detect sharp edges
        //  and reject bad profiles.
    }

    /**
     * Root out too small stars.
     */
    private fun isTooSmall(bin: StarPixelBin): Boolean {
        return bin.pixelCount < 4
    }

    /**
     * Root out too large stars.
     */
    private fun isTooLarge(bin: StarPixelBin, image: Image): Boolean {
        val maxSize = (max(image.width, image.height) * 0.04).toInt()
        return bin.dimensions[0] > maxSize || bin.dimensions[1] > maxSize
    }

    /**
     * Analyze if the pixel bin is streakish (star / satellite streak).
     */
    private fun isStreakish(bin: StarPixelBin): Boolean {
        val (width, height) = bin.dimensions
        return width == 1 || height == 1 ||
                width.toFloat() / height >= 6f || height / width.toFloat() >= 6f
    }

    /**
     * Measures star roundness by calculating how many pixels occupy the square area
     * the star occupies. When perfectly round, the ratio should be 1.
     */
    private fun isNotRound(bin: StarPixelBin): Boolean {
        val perimeter = PI * max(bin.dimensions[0], bin.dimensions[1])
        val ratio = (2 * TAU * bin.pixelCount) / (perimeter * perimeter)
        return ratio < 0.25
    }
}
