package nebulosa.image.algorithms.transformation.correction

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.image.format.ImageChannel

data class FlatCorrection(private val flatFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == flatFrame.width) { "calibration image width does not match source width" }
        require(source.height == flatFrame.height) { "calibration image height does not match source height" }

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]
            val stats = source.compute(Statistics(channel, noMedian = true, noDeviation = true))

            for (k in 0 until source.size) {
                val light = source.read(k, channel)
                val flat = flatFrame.read(k, channel)
                val normed = flat / stats.mean
                source.write(k, channel, light / normed)
            }
        }

        return source
    }
}
