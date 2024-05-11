package nebulosa.image.algorithms.transformation

import nebulosa.fits.FitsHeaderCard
import nebulosa.fits.FitsKeyword
import nebulosa.fits.cfaPattern
import nebulosa.image.format.HeaderCard
import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ReadableHeader

enum class CfaPattern(private val pattern: Array<Array<ImageChannel>>, code: String) :
    HeaderCard by FitsHeaderCard.create(FitsKeyword.BAYERPAT, code) {
    RGGB(arrayOf(arrayOf(ImageChannel.RED, ImageChannel.GREEN), arrayOf(ImageChannel.GREEN, ImageChannel.BLUE)), "RGGB"),
    BGGR(arrayOf(arrayOf(ImageChannel.BLUE, ImageChannel.GREEN), arrayOf(ImageChannel.GREEN, ImageChannel.RED)), "BGGR"),
    GBRG(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.BLUE), arrayOf(ImageChannel.RED, ImageChannel.GREEN)), "GBRG"),
    GRBG(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.RED), arrayOf(ImageChannel.BLUE, ImageChannel.GREEN)), "GRBG"),
    GRGB(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.RED), arrayOf(ImageChannel.GREEN, ImageChannel.BLUE)), "GRGB"),
    GBGR(arrayOf(arrayOf(ImageChannel.GREEN, ImageChannel.BLUE), arrayOf(ImageChannel.GREEN, ImageChannel.RED)), "GBGR"),
    RGBG(arrayOf(arrayOf(ImageChannel.RED, ImageChannel.GREEN), arrayOf(ImageChannel.BLUE, ImageChannel.GREEN)), "RGBG"),
    BGRG(arrayOf(arrayOf(ImageChannel.BLUE, ImageChannel.GREEN), arrayOf(ImageChannel.RED, ImageChannel.GREEN)), "BGRG");

    operator fun get(y: Int, x: Int) = pattern[y][x]

    companion object {

        @JvmStatic
        fun from(header: ReadableHeader): CfaPattern? {
            return header.cfaPattern?.let(CfaPattern::valueOf)
        }
    }
}
