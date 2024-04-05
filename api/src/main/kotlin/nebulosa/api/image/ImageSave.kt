package nebulosa.api.image

import nebulosa.fits.Bitpix
import java.nio.file.Path

data class ImageSave(
    val format: ImageFormat = ImageFormat.FITS,
    val bitpix: Bitpix = Bitpix.BYTE,
    val transformation: ImageTransformation = ImageTransformation.EMPTY,
    val path: Path = EMPTY_PATH,
) {

    companion object {

        @JvmStatic private val EMPTY_PATH = Path.of("")
    }
}
