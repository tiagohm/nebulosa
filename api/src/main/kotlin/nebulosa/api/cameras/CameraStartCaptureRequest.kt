package nebulosa.api.cameras

import nebulosa.api.converters.time.DurationUnit
import nebulosa.api.guiding.DitherAfterExposureRequest
import nebulosa.api.livestacker.LiveStackingRequest
import nebulosa.api.stacker.StackerGroupType
import nebulosa.api.validators.*
import nebulosa.indi.device.camera.FrameType
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class CameraStartCaptureRequest(
    @JvmField val enabled: Boolean = true,
    // Capture.
    @JvmField val exposureTime: Duration = Duration.ZERO,
    @JvmField val exposureAmount: Int = 1, // 0 = looping
    @field:DurationUnit(ChronoUnit.SECONDS) @JvmField val exposureDelay: Duration = Duration.ZERO,
    @JvmField val x: Int = 0,
    @JvmField val y: Int = 0,
    @JvmField val width: Int = 0,
    @JvmField val height: Int = 0,
    @JvmField val frameFormat: String? = null,
    @JvmField val frameType: FrameType = FrameType.LIGHT,
    @JvmField val binX: Int = 1,
    @JvmField val binY: Int = 1,
    @JvmField val gain: Int = 0,
    @JvmField val offset: Int = 0,
    @JvmField val autoSave: Boolean = false,
    @JvmField val savePath: Path? = null,
    @JvmField val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
    @JvmField val calibrationGroup: String? = null,
    // Dithering.
    @JvmField val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
    // Stacking.
    @JvmField val liveStacking: LiveStackingRequest = LiveStackingRequest.DISABLED,
    @JvmField val stackerGroupType: StackerGroupType = StackerGroupType.MONO,
    // Filter Wheel.
    @JvmField val filterPosition: Int = 0,
    @JvmField val shutterPosition: Int = 0,
    // Focuser.
    @JvmField val focusOffset: Int = 0,
    // Others.
    @JvmField val namingFormat: CameraCaptureNamingFormat = CameraCaptureNamingFormat.DEFAULT,
) : Validatable {

    inline val isLoop
        get() = exposureAmount <= 0

    override fun validate() {
        exposureTime.min(1L, TimeUnit.MICROSECONDS).max(1L, TimeUnit.HOURS)
        exposureAmount.range(0, MAX_EXPOSURE_AMOUNT)
        exposureDelay.positiveOrZero().max(1L, TimeUnit.MINUTES)
        x.positiveOrZero()
        y.positiveOrZero()
        width.positiveOrZero()
        height.positiveOrZero()
        binX.positive()
        binY.positive()
        gain.positiveOrZero()
        offset.positiveOrZero()
        dither.validate()
    }

    companion object {

        const val MAX_EXPOSURE_AMOUNT = 10000

        @JvmStatic val EMPTY = CameraStartCaptureRequest()
    }
}
