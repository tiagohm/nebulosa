package nebulosa.imaging.algorithms

import nebulosa.fits.clone
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nom.tam.fits.header.extra.SBFitsExt
import kotlin.math.abs
import kotlin.math.min

class DarkSubtraction(private val darkFrame: Image) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        val header = source.header.clone()
        val size = source.width * source.height
        val calibrated = Image(source.width, source.height, header, source.mono)
        val pedestals = FloatArray(source.numberOfChannels)

        for (i in 0 until source.numberOfChannels) {
            val channel = ImageChannel.RGB[i]

            for (k in 0 until size) {
                val light = source.read(k, channel)
                val dark = darkFrame.read(k, channel)
                val p = light - dark
                if (p < 0f) pedestals[i] = min(pedestals[i], p)
                calibrated.write(k, channel, p)
            }
        }

        val pedestal = abs(pedestals.min())

        if (pedestal > 0f) {
            for (i in 0 until source.numberOfChannels) {
                val channel = ImageChannel.RGB[i]

                for (k in 0 until size) {
                    val light = source.read(k, channel)
                    calibrated.write(k, channel, light + pedestal)
                }
            }

            header.addValue(SBFitsExt.PEDESTAL, pedestal)
        }

        return source
    }
}
