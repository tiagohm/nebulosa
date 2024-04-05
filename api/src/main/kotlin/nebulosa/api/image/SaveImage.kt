package nebulosa.api.image

import nebulosa.fits.Bitpix
import java.nio.file.Path

data class SaveImage(
    val format: ImageExtension = ImageExtension.FITS,
    val bitpix: Bitpix = Bitpix.BYTE,
    val transformation: ImageTransformation = ImageTransformation.EMPTY,
    val path: Path? = null,
)
