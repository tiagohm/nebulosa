package nebulosa.imaging.algorithms

import nebulosa.fits.FitsKeywords
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import kotlin.math.abs
import kotlin.math.min

class DarkSubtraction(private val darkFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        require(source.width == darkFrame.width) { "calibration image width does not match source width" }
        require(source.height == darkFrame.height) { "calibration image height does not match source height" }

        val size = source.width * source.height
        val pedestals = FloatArray(source.numberOfChannels)

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]

            for (k in 0 until size) {
                val light = source.read(k, channel)
                val dark = darkFrame.read(k, channel)
                val p = light - dark
                if (p < 0f) pedestals[i] = min(pedestals[i], p)
                source.write(k, channel, p)
            }
        }

        val pedestal = abs(pedestals.min())

        if (pedestal > 0f) {
            for (i in 0 until source.numberOfChannels) {
                val channel = ImageChannel.RGB[i]

                for (k in 0 until size) {
                    val light = source.read(k, channel)
                    source.write(k, channel, light + pedestal)
                }
            }

            source.header.addValue(FitsKeywords.PEDESTAL, -(pedestal * 65535).toInt())
        }

        return source
    }
}
