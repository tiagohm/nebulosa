package nebulosa.indi.device.camera

import nebulosa.fits.Fits
import java.io.InputStream

data class CameraFrameCaptured(
    override val device: Camera,
    @JvmField val stream: InputStream? = null,
    @JvmField val image: Fits? = null,
) : CameraEvent
