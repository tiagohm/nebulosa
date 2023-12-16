package nebulosa.api.sequencer

import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import java.time.Duration

data class AutoFocusAfterConditions(
    val onStart: Boolean = false,
    val onFilterChange: Boolean = false,
    @field:DurationMin(seconds = 0) @field:DurationMax(hours = 8) val afterElapsedTime: Duration = Duration.ZERO,
    @field:PositiveOrZero val afterExposures: Int = 0,
    @field:PositiveOrZero val afterTemperatureChange: Double = 0.0,
    @field:PositiveOrZero val afterHFDIncrease: Double = 0.0,
) {

    companion object {

        @JvmStatic val DISABLED = AutoFocusAfterConditions()
    }
}
