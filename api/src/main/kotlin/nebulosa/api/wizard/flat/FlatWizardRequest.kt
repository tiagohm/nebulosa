package nebulosa.api.wizard.flat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.beans.converters.time.DurationUnit
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.max
import nebulosa.api.javalin.min
import nebulosa.api.javalin.range
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class FlatWizardRequest(
    @JsonIgnoreProperties("camera", "focuser", "dither") @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMin: Duration = MIN_EXPOSURE,
    @field:DurationUnit(ChronoUnit.MILLIS) @JvmField val exposureMax: Duration = MAX_EXPOSURE,
    @JvmField val meanTarget: Int = 32768, // 50% = 32768 (16-bit)
    @JvmField val meanTolerance: Int = 10, // 10%
) : Validatable {

    override fun validate() {
        capture.validate()
        exposureMin.min(1L, TimeUnit.MILLISECONDS).max(10L, TimeUnit.MINUTES)
        exposureMax.min(1L, TimeUnit.MILLISECONDS).max(10L, TimeUnit.MINUTES)
        meanTarget.range(0, 65535)
        meanTolerance.range(0, 100)
    }

    companion object {

        @JvmStatic val MIN_EXPOSURE = Duration.ofMillis(1)!!
        @JvmStatic val MAX_EXPOSURE = Duration.ofSeconds(2)!!
    }
}
