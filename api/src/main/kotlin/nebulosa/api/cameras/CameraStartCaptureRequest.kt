package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.guiding.DitherAfterExposureRequest
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.hibernate.validator.constraints.Range
import java.nio.file.Path
import java.time.Duration

data class CameraStartCaptureRequest(
    @JsonIgnore val camera: Camera? = null,
    @field:Positive val exposureTime: Duration = Duration.ZERO,
    @field:Range(min = 0L, max = 1000L) val exposureAmount: Int = 1, // 0 = looping
    @field:Range(min = 0L, max = 60L) val exposureDelay: Duration = Duration.ZERO,
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
    @JsonIgnore val saveInMemory: Boolean = savePath == null,
    @field:Valid val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
) {

    inline val isLoop
        get() = exposureAmount <= 0
}
