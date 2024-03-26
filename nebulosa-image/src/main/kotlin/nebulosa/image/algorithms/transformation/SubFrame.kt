package nebulosa.image.algorithms.transformation

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeywordDictionary
import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm

data class SubFrame(
    private val x: Int, private val y: Int,
    private val width: Int, private val height: Int,
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

        val newHeader = FitsHeader(source.header)
        newHeader.add(FitsKeywordDictionary.NAXIS1, width)
        newHeader.add(FitsKeywordDictionary.NAXIS2, height)
        val subframe = Image(width, height, newHeader, source.mono)

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

    companion object {

        @JvmStatic
        fun centered(x: Int, y: Int, width: Int, height: Int): SubFrame {
            return SubFrame(x - width / 2, y - height / 2, width, height)
        }

        @JvmStatic
        fun centered(x: Int, y: Int, radius: Int): SubFrame {
            return SubFrame(x - radius, y - radius, radius * 2, radius * 2)
        }
    }
}
