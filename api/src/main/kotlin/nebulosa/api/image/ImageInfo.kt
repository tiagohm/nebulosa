package nebulosa.api.image

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.converters.angle.DeclinationSerializer
import nebulosa.api.converters.angle.RightAscensionSerializer
import nebulosa.fits.Bitpix
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class ImageInfo(
    @JvmField val path: Path,
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val mono: Boolean,
    @JvmField val stretch: ImageTransformation.Stretch,
    @field:JsonSerialize(using = RightAscensionSerializer::class) @JvmField val rightAscension: Double? = null,
    @field:JsonSerialize(using = DeclinationSerializer::class) @JvmField val declination: Double? = null,
    @JvmField val solved: ImageSolved? = null,
    @JvmField val headers: List<ImageHeaderItem> = emptyList(),
    @JvmField val bitpix: Bitpix = Bitpix.BYTE,
    @JvmField val camera: Camera? = null,
    @JsonIgnoreProperties("histogram") @JvmField val statistics: Statistics.Data? = null,
)
