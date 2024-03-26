package nebulosa.fits

import nebulosa.image.format.ImageData
import nebulosa.image.format.ImageHdu
import nebulosa.image.format.ReadableHeader

internal data class SeekableSourceImageHdu(
    override val header: ReadableHeader,
    override val data: ImageData,
) : ImageHdu {

    override val width = header.width
    override val height = header.height
    override val numberOfChannels = header.numberOfChannels
}
