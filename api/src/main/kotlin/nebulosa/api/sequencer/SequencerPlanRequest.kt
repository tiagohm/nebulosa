package nebulosa.api.sequencer

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureNamingFormat
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.converters.time.DurationUnit
import nebulosa.api.focusers.BacklashCompensation
import nebulosa.api.guiding.DitherAfterExposureRequest
import nebulosa.api.livestacker.LiveStackingRequest
import nebulosa.api.validators.Validatable
import nebulosa.api.validators.max
import nebulosa.api.validators.notEmpty
import nebulosa.api.validators.positiveOrZero
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class SequencerPlanRequest(
    @JvmField @field:DurationUnit(ChronoUnit.SECONDS) val initialDelay: Duration = Duration.ZERO,
    @JvmField val captureMode: SequencerCaptureMode = SequencerCaptureMode.INTERLEAVED,
    @JvmField val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
    @JvmField val savePath: Path? = null,
    @JvmField val sequences: List<CameraStartCaptureRequest> = emptyList(),
    @JvmField val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
    @JvmField val autoFocus: AutoFocusAfterConditions = AutoFocusAfterConditions.DISABLED,
    @JvmField val liveStacking: LiveStackingRequest = LiveStackingRequest.DISABLED,
    @JvmField val namingFormat: CameraCaptureNamingFormat = CameraCaptureNamingFormat.DEFAULT,
    @JvmField val backlashCompensation: BacklashCompensation = BacklashCompensation.EMPTY,
) : Validatable {

    override fun validate() {
        initialDelay.positiveOrZero().max(60L, TimeUnit.MINUTES)
        sequences.notEmpty()
        dither.validate()
        autoFocus.validate()
        liveStacking.validate()
    }
}
