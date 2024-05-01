package nebulosa.api.sequencer

import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.time.DurationMax
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

data class AutoFocusAfterConditions(
    @JvmField val enabled: Boolean = false,
    @JvmField val onStart: Boolean = false,
    @JvmField val onFilterChange: Boolean = false,
    @field:DurationMin(seconds = 0) @field:DurationMax(hours = 8) @field:DurationUnit(ChronoUnit.SECONDS) @JvmField val afterElapsedTime: Duration = Duration.ZERO,
    @field:PositiveOrZero @JvmField val afterExposures: Int = 0,
    @field:PositiveOrZero @JvmField val afterTemperatureChange: Double = 0.0,
    @field:PositiveOrZero @JvmField val afterHFDIncrease: Double = 0.0,
    @JvmField val afterElapsedTimeEnabled: Boolean = false,
    @JvmField val afterExposuresEnabled: Boolean = false,
    @JvmField val afterTemperatureChangeEnabled: Boolean = false,
    @JvmField val afterHFDIncreaseEnabled: Boolean = false,
) {

    companion object {

        @JvmStatic val DISABLED = AutoFocusAfterConditions()
    }
}
