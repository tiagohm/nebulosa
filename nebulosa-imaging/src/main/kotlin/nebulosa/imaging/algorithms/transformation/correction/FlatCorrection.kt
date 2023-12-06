package nebulosa.imaging.algorithms.transformation.correction

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.TransformAlgorithm
import nebulosa.imaging.algorithms.computation.Statistics

class FlatCorrection(private val flatFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == flatFrame.width) { "calibration image width does not match source width" }
        require(source.height == flatFrame.height) { "calibration image height does not match source height" }

        val size = source.width * source.height

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]
            val stats = source.compute(Statistics(channel, noMedian = true, noDeviation = true))

            for (k in 0 until size) {
                val light = source.read(k, channel)
                val flat = flatFrame.read(k, channel)
                val normed = flat / stats.mean
                source.write(k, channel, light / normed)
            }
        }

        return source
    }
}
