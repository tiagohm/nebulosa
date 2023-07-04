package nebulosa.api.data.requests

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.indi.device.camera.FrameType

data class CameraStartCaptureRequest(
    @field:Positive val exposure: Long,
    @field:Positive val amount: Int = 1,
    @field:PositiveOrZero val delay: Long = 0,
    @field:PositiveOrZero val x: Int = 0,
    @field:PositiveOrZero val y: Int = 0,
    @field:PositiveOrZero val width: Int = 0,
    @field:PositiveOrZero val height: Int = 0,
    val frameFormat: String? = null,
    val frameType: FrameType = FrameType.LIGHT,
    @field:Positive val binX: Int = 1,
    @field:Positive val binY: Int = 1,
    @field:PositiveOrZero val gain: Int = 0,
    @field:PositiveOrZero val offset: Int = 0,
)
