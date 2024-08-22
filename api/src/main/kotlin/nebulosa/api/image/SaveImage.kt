package nebulosa.api.image

import nebulosa.fits.Bitpix
import java.nio.file.Path

data class SaveImage(
    @JvmField val format: ImageExtension = ImageExtension.FITS,
    @JvmField val bitpix: Bitpix = Bitpix.BYTE,
    @JvmField val shouldBeTransformed: Boolean = true,
    @JvmField val transformation: ImageTransformation = ImageTransformation.EMPTY,
    @JvmField val path: Path? = null,
    @JvmField val subFrame: ROI = ROI.EMPTY,
)
