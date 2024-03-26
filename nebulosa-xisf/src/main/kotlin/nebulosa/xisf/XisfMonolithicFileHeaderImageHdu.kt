package nebulosa.xisf

import nebulosa.fits.FitsHeader
import nebulosa.image.format.ImageHdu
import nebulosa.io.SeekableSource

internal data class XisfMonolithicFileHeaderImageHdu(
    private val image: XisfMonolithicFileHeader.Image,
    private val source: SeekableSource,
) : ImageHdu {

    override val width
        get() = image.width

    override val height
        get() = image.height

    override val numberOfChannels
        get() = image.numberOfChannels

    override val header = FitsHeader(image.keywords)

    override val data = XisfMonolithicFileHeaderImageData(image, source)
}
