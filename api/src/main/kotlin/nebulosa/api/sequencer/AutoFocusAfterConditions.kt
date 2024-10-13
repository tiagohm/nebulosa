package nebulosa.api.sequencer

import nebulosa.api.converters.time.DurationUnit
import nebulosa.api.validators.Validatable
import nebulosa.api.validators.max
import nebulosa.api.validators.positiveOrZero
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class AutoFocusAfterConditions(
    @JvmField val enabled: Boolean = false,
    @JvmField val onStart: Boolean = false,
    @JvmField val onFilterChange: Boolean = false,
    @field:DurationUnit(ChronoUnit.SECONDS) @JvmField val afterElapsedTime: Duration = Duration.ZERO,
    @JvmField val afterExposures: Int = 0,
    @JvmField val afterTemperatureChange: Double = 0.0,
    @JvmField val afterHFDIncrease: Double = 0.0,
    @JvmField val afterElapsedTimeEnabled: Boolean = false,
    @JvmField val afterExposuresEnabled: Boolean = false,
    @JvmField val afterTemperatureChangeEnabled: Boolean = false,
    @JvmField val afterHFDIncreaseEnabled: Boolean = false,
) : Validatable {

    override fun validate() {
        afterElapsedTime.positiveOrZero().max(24L, TimeUnit.HOURS)
        afterExposures.positiveOrZero()
        afterTemperatureChange.positiveOrZero()
        afterHFDIncrease.positiveOrZero()
    }

    companion object {

        @JvmStatic val DISABLED = AutoFocusAfterConditions()
    }
}
