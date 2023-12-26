package nebulosa.api.sequencer

import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

data class AutoFocusAfterConditions(
    val enabled: Boolean = false,
    val onStart: Boolean = false,
    val onFilterChange: Boolean = false,
    @field:DurationMin(seconds = 0) @field:DurationMax(hours = 8) @field:DurationUnit(ChronoUnit.SECONDS) val afterElapsedTime: Duration = Duration.ZERO,
    @field:PositiveOrZero val afterExposures: Int = 0,
    @field:PositiveOrZero val afterTemperatureChange: Double = 0.0,
    @field:PositiveOrZero val afterHFDIncrease: Double = 0.0,
    val afterElapsedTimeEnabled: Boolean = false,
    val afterExposuresEnabled: Boolean = false,
    val afterTemperatureChangeEnabled: Boolean = false,
    val afterHFDIncreaseEnabled: Boolean = false,
) {

    companion object {

        @JvmStatic val DISABLED = AutoFocusAfterConditions()
    }
}
