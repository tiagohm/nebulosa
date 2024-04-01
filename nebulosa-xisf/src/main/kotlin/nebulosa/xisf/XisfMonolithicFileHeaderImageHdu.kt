package nebulosa.xisf

import nebulosa.fits.FitsHeader
import nebulosa.image.format.ImageHdu
import nebulosa.io.SeekableSource

internal data class XisfMonolithicFileHeaderImageHdu(
    private val image: XisfMonolithicFileHeader.Image,
    private val source: SeekableSource,
) : ImageHdu {

    override val width = image.width
    override val height = image.height
    override val numberOfChannels = image.numberOfChannels
    override val header = image.keywords
    override val data = XisfMonolithicFileHeaderImageData(image, source)
}
