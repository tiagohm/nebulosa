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
    @JsonIgnoreProperties("camera", "focuser", "dither") @JvmField val capture: CameraStartCaptureRequest,
    @field:DurationMin(millis = 1) @field:DurationMax(minutes = 1) @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMin: Duration = MIN_EXPOSURE,
    @field:DurationMin(millis = 1) @field:DurationMax(minutes = 1) @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMax: Duration = MAX_EXPOSURE,
    @field:Range(min = 0, max = 65535) @JvmField val meanTarget: Int = 32768, // 50% = 32768 (16-bit)
    @field:Range(min = 0, max = 100) @JvmField val meanTolerance: Int = 10, // 10%
) {

    companion object {

        @JvmStatic val MIN_EXPOSURE = Duration.ofMillis(1)!!
        @JvmStatic val MAX_EXPOSURE = Duration.ofSeconds(2)!!
    }
}
