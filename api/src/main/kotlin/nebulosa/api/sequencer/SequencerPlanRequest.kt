package nebulosa.api.sequencer

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureNamingFormat
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.DitherAfterExposureRequest
import nebulosa.api.livestacker.LiveStackingRequest
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class SequencerPlanRequest(
    @JvmField @field:DurationUnit(ChronoUnit.SECONDS) @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 60) val initialDelay: Duration = Duration.ZERO,
    @JvmField val captureMode: SequencerCaptureMode = SequencerCaptureMode.INTERLEAVED,
    @JvmField val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
    @JvmField val savePath: Path? = null,
    @JvmField @field:NotEmpty val sequences: List<CameraStartCaptureRequest> = emptyList(),
    @JvmField @field:Valid val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
    @JvmField @field:Valid val autoFocus: AutoFocusAfterConditions = AutoFocusAfterConditions.DISABLED,
    @JvmField @field:Valid val liveStacking: LiveStackingRequest = LiveStackingRequest.DISABLED,
    @JvmField @field:Valid val namingFormat: CameraCaptureNamingFormat = CameraCaptureNamingFormat.DEFAULT,
)
