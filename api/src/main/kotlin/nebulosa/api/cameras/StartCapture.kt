package nebulosa.api.cameras

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.indi.devices.cameras.FrameType
import org.jetbrains.annotations.NotNull

data class StartCapture(
    @field:NotNull @field:Positive val exposure: Long,
    @field:NotNull @field:Positive val amount: Int,
    @field:NotNull @field:Min(100L) val delay: Long,
    @field:NotNull @field:PositiveOrZero val x: Int,
    @field:NotNull @field:PositiveOrZero val y: Int,
    @field:NotNull @field:Positive val width: Int,
    @field:NotNull @field:Positive val height: Int,
    @field:NotBlank val frameFormat: String,
    @field:NotNull val frameType: FrameType,
    @field:NotNull @field:Positive val binX: Int,
    @field:NotNull @field:Positive val binY: Int,
)
