package nebulosa.image.algorithms.transformation

import nebulosa.fits.cfaPattern
import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ReadableHeader

enum class CfaPattern(private val pattern: Array<Array<ImageChannel>>) {
    RGGB(arrayOf(arrayOf(ImageChannel.RED, ImageChannel.GREEN), arrayOf(ImageChannel.GREEN, ImageChannel.BLUE))),
    BGGR(arrayOf(arrayOf(ImageChannel.BLUE, ImageChannel.GREEN), arrayOf(ImageChannel.GREEN, ImageChannel.RED))),
    GBRG(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.BLUE), arrayOf(ImageChannel.RED, ImageChannel.GREEN))),
    GRBG(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.RED), arrayOf(ImageChannel.BLUE, ImageChannel.GREEN))),
    GRGB(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.RED), arrayOf(ImageChannel.GREEN, ImageChannel.BLUE))),
    GBGR(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.BLUE), arrayOf(ImageChannel.GREEN, ImageChannel.RED))),
    RGBG(arrayOf(arrayOf(ImageChannel.RED, ImageChannel.GREEN), arrayOf(ImageChannel.BLUE, ImageChannel.GREEN))),
    BGRG(arrayOf(arrayOf(ImageChannel.BLUE, ImageChannel.GREEN), arrayOf(ImageChannel.RED, ImageChannel.GREEN)));

    operator fun get(y: Int, x: Int) = pattern[y][x]

    companion object {

        @JvmStatic
        fun from(header: ReadableHeader): CfaPattern? {
            return header.cfaPattern?.let(CfaPattern::valueOf)
        }
    }
}