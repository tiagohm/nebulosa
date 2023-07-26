package nebulosa.imaging.algorithms

import nebulosa.fits.FitsKeywords
import nebulosa.fits.clone
import nebulosa.imaging.Image

class SubFrame(
    private val x: Int, private val y: Int,
    private val width: Int, private val height: Int
) : TransformAlgorithm {

    init {
        require(x >= 0) { "x < 0: $x" }
        require(y >= 0) { "y < 0: $y" }
        require(width > 0) { "width <= 0: $width" }
        require(height > 0) { "height <= 0: $height" }
    }

    override fun transform(source: Image): Image {
        require(x + width <= source.width) { "subframe.width < source.width: ${x + width} > ${source.width}" }
        require(y + height <= source.height) { "subframe.height < source.height: ${y + height} > ${source.height}" }

        val header = source.header.clone()
        header.addValue(FitsKeywords.NAXISn.n(1), width)
        header.addValue(FitsKeywords.NAXISn.n(2), height)
        val subframe = Image(width, height, header, source.mono)

        var index = 0

        for (n in y until y + height) {
            val row = source.indexAt(x, n)

            for (m in 0 until width) {
                for (i in 0 until source.numberOfChannels) {
                    subframe.write(index, i, source.read(row + m, i))
                }

                index++
            }
        }

        return subframe
    }
}
