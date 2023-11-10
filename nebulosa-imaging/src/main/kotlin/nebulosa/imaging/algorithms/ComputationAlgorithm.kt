package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel

fun interface ComputationAlgorithm<out T> {

    fun compute(source: Image): T

    companion object {

        internal inline fun Image.sampling(
            channel: ImageChannel = ImageChannel.GRAY,
            sampleBy: Int = 1,
            computation: (Float) -> Unit,
        ): Int {
            var count = 0

            for (i in indices step sampleBy) {
                val pixel = when (channel) {
                    ImageChannel.GRAY -> readGray(i)
                    ImageChannel.RED -> readRed(i)
                    ImageChannel.GREEN -> readGreen(i)
                    else -> readBlue(i)
                }

                if (pixel >= 0.0 && pixel.isFinite()) {
                    computation(pixel)
                    count++
                }
            }

            return count
        }
    }
}
