package nebulosa.api.cameras

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.guiding.DitherAfterExposureRequest
import nebulosa.api.livestacker.LiveStackingRequest
import nebulosa.indi.device.camera.FrameType
import org.hibernate.validator.constraints.Range
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class CameraStartCaptureRequest(
    @JvmField val enabled: Boolean = true,
    // Capture.
    @field:DurationMin(nanos = 1000L) @field:DurationMax(minutes = 60L) @JvmField val exposureTime: Duration = Duration.ZERO,
    @field:Range(min = 0L, max = 1000L) @JvmField val exposureAmount: Int = 1, // 0 = looping
    @field:DurationUnit(ChronoUnit.SECONDS) @field:DurationMin(nanos = 0L) @field:DurationMax(seconds = 60L)
    @JvmField val exposureDelay: Duration = Duration.ZERO,
    @field:PositiveOrZero @JvmField val x: Int = 0,
    @field:PositiveOrZero @JvmField val y: Int = 0,
    @field:PositiveOrZero @JvmField val width: Int = 0,
    @field:PositiveOrZero @JvmField val height: Int = 0,
    @JvmField val frameFormat: String? = null,
    @JvmField val frameType: FrameType = FrameType.LIGHT,
    @field:Positive @JvmField val binX: Int = 1,
    @field:Positive @JvmField val binY: Int = 1,
    @field:PositiveOrZero @JvmField val gain: Int = 0,
    @field:PositiveOrZero @JvmField val offset: Int = 0,
    @JvmField val autoSave: Boolean = false,
    @JvmField val savePath: Path? = null,
    @JvmField val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
    @field:Valid @JvmField val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
    @JvmField val calibrationGroup: String? = null,
    @JvmField val liveStacking: LiveStackingRequest = LiveStackingRequest.DISABLED,
    // Filter Wheel.
    @JvmField val filterPosition: Int = 0,
    @JvmField val shutterPosition: Int = 0,
    // Focuser.
    @JvmField val focusOffset: Int = 0,
    // Others.
    @JvmField val namingFormat: CameraCaptureNamingFormat = CameraCaptureNamingFormat.DEFAULT,
) {

    inline val isLoop
        get() = exposureAmount <= 0

    companion object {

        @JvmStatic val EMPTY = CameraStartCaptureRequest()
    }
}
