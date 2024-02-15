package nebulosa.indi.device.camera

import nebulosa.fits.Fits
import java.io.InputStream

data class CameraFrameCaptured(
    override val device: Camera,
    @JvmField val stream: InputStream?,
    @JvmField val fits: Fits?,
    @JvmField val compressed: Boolean,
) : CameraEvent
