package nebulosa.image.algorithms.transformation.correction

import nebulosa.image.Image
import nebulosa.image.ImageChannel
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max

data class DarkSubtraction(private val darkFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == darkFrame.width) { "calibration image width does not match source width" }
        require(source.height == darkFrame.height) { "calibration image height does not match source height" }

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]

            for (k in 0 until source.size) {
                val light = source.read(k, channel)
                val dark = darkFrame.read(k, channel)
                source.write(k, channel, max(0f, light - dark))
            }
        }

        return source
    }
}
