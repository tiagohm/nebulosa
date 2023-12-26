package nebulosa.api.sequencer

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.DitherAfterExposureRequest
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit

data class SequencePlanRequest(
    @field:DurationUnit(ChronoUnit.SECONDS) @field:DurationMin(seconds = 0) @field:DurationMax(minutes = 60) val initialDelay: Duration = Duration.ZERO,
    @field:Valid val captureMode: SequenceCaptureMode = SequenceCaptureMode.INTERLEAVED,
    val savePath: Path? = null,
    @field:NotEmpty val entries: List<CameraStartCaptureRequest> = emptyList(),
    @field:Valid val dither: DitherAfterExposureRequest = DitherAfterExposureRequest.DISABLED,
    @field:Valid val autoFocus: AutoFocusAfterConditions = AutoFocusAfterConditions.DISABLED,
)
