package nebulosa.api.image

import nebulosa.fits.*
import nebulosa.image.format.ReadableHeader
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.camera.FrameType.Companion.frameType

data class ImageAnalyzed(
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val binX: Int,
    @JvmField val binY: Int,
    @JvmField val gain: Double,
    @JvmField val exposureTime: Long,
    @JvmField val type: FrameType,
    @JvmField val filter: ImageFilterType,
) {

    constructor(header: ReadableHeader) : this(
        header.width, header.height, header.binX, header.binY, header.gain, header.exposureTimeInMicroseconds,
        header.frameType ?: FrameType.LIGHT, ImageFilterType.from(header)
    )
}
