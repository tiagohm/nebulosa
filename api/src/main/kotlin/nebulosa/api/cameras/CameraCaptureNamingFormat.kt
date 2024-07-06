package nebulosa.api.cameras

import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.BIAS_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.DARK_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.FLAT_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.LIGHT_FORMAT
import nebulosa.indi.device.camera.FrameType

data class CameraCaptureNamingFormat(
    @JvmField val light: String? = null,
    @JvmField val dark: String? = null,
    @JvmField val flat: String? = null,
    @JvmField val bias: String? = null,
) {

    fun formatFor(type: FrameType) = when (type) {
        FrameType.LIGHT -> light?.ifBlank { null } ?: LIGHT_FORMAT
        FrameType.DARK -> dark?.ifBlank { null } ?: DARK_FORMAT
        FrameType.FLAT -> flat?.ifBlank { null } ?: FLAT_FORMAT
        FrameType.BIAS -> bias?.ifBlank { null } ?: BIAS_FORMAT
    }

    companion object {

        @JvmStatic val EMPTY = CameraCaptureNamingFormat()
        @JvmStatic val DEFAULT = CameraCaptureNamingFormat(LIGHT_FORMAT, DARK_FORMAT, FLAT_FORMAT, BIAS_FORMAT)
    }
}
