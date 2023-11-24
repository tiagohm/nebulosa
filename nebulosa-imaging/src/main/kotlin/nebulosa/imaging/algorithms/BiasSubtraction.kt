package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import kotlin.math.max

class BiasSubtraction(private val biasFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == biasFrame.width) { "calibration image width does not match source width" }
        require(source.height == biasFrame.height) { "calibration image height does not match source height" }

        val size = source.width * source.height

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]

            for (k in 0 until size) {
                val light = source.read(k, channel)
                val bias = biasFrame.read(k, channel)
                source.write(k, channel, max(0f, light - bias))
            }
        }

        return source
    }
}