package nebulosa.api.wizard.flat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.converters.time.DurationUnit
import nebulosa.api.validators.Validatable
import nebulosa.api.validators.range
import java.time.Duration
import java.time.temporal.ChronoUnit

data class FlatWizardRequest(
    @JsonIgnoreProperties("camera", "focuser", "dither") @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMin: Duration = MIN_EXPOSURE,
    @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMax: Duration = DEFAULT_EXPOSURE,
    @JvmField val meanTarget: Int = 32768, // 50% = 32768 (16-bit)
    @JvmField val meanTolerance: Int = 10, // 10%
    @JvmField val filters: IntArray = IntArray(0),
) : Validatable {

    override fun validate() {
        capture.validate()
        exposureMin.range(MIN_EXPOSURE, MAX_EXPOSURE)
        exposureMax.range(MIN_EXPOSURE, MAX_EXPOSURE)
        meanTarget.range(0, 65535)
        meanTolerance.range(0, 100)
    }

    companion object {

        val MIN_EXPOSURE = Duration.ofMillis(1)!!
        val DEFAULT_EXPOSURE = Duration.ofSeconds(2)!!
        val MAX_EXPOSURE = Duration.ofMinutes(1)!!
    }
}
