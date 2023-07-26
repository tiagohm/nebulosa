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

            for (i in 0 until width * height step sampleBy) {
                val pixel = when (channel) {
                    ImageChannel.GRAY -> readGray(i)
                    ImageChannel.RED -> readRed(i)
                    ImageChannel.GREEN -> readGreen(i)
                    else -> readBlue(i)
                }

                computation(pixel)

                count++
            }

            return count
        }
    }
}
