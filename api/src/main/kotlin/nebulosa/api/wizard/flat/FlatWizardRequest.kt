package nebulosa.api.wizard.flat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraStartCaptureRequest
import org.hibernate.validator.constraints.Range
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

data class FlatWizardRequest(
    @JsonIgnoreProperties("focuser", "dither") val captureRequest: CameraStartCaptureRequest,
    @field:DurationMin(millis = 1) @field:DurationMax(minutes = 1) @field:DurationUnit(ChronoUnit.MILLIS) val exposureMin: Duration = MIN_EXPOSURE,
    @field:DurationMin(millis = 1) @field:DurationMax(minutes = 1) @field:DurationUnit(ChronoUnit.MILLIS) val exposureMax: Duration = MAX_EXPOSURE,
    @field:Range(min = 0, max = 1) val meanTarget: Float = 0.5f, // 50% = 32768 (16-bit)
    @field:Range(min = 0, max = 1) val meanTolerance: Float = 0.1f, // 10%
) {

    companion object {

        @JvmStatic val MIN_EXPOSURE = Duration.ofMillis(1)!!
        @JvmStatic val MAX_EXPOSURE = Duration.ofSeconds(2)!!
    }
}
