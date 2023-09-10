package nebulosa.api.cameras

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.indi.device.camera.FrameType
import org.hibernate.validator.constraints.Range
import java.nio.file.Path

data class CameraStartCaptureRequest(
    @field:Positive val exposureInMicroseconds: Long = 0L,
    @field:Range(min = 0L, max = 1000L) val exposureAmount: Int = 1, // 0 = looping
    @field:Range(min = 0L, max = 60L) val exposureDelayInSeconds: Long = 0L,
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
    val autoSave: Boolean = false,
    val savePath: Path? = null,
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
)
