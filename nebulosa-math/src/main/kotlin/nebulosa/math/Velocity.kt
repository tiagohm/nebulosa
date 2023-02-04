package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.DAYSEC

/**
 * Represents a velocity [value] in au/day.
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Velocity(val value: Double) {

    /**
     * Converts this velocity to km/s.
     */
    inline val kms
        get() = value * AU_KM / DAYSEC

    /**
     * Converts this velocity to m/s.
     */
    inline val ms
        get() = value * AU_M / DAYSEC

    inline operator fun plus(velocity: Velocity) = (value + velocity.value).auDay

    inline operator fun plus(velocity: Double) = (value + velocity).auDay

    inline operator fun plus(velocity: Int) = (value + velocity).auDay

    inline operator fun minus(velocity: Velocity) = (value - velocity.value).auDay

    inline operator fun minus(velocity: Double) = (value - velocity).auDay

    inline operator fun minus(velocity: Int) = (value - velocity).auDay

    inline operator fun times(velocity: Velocity) = (value * velocity.value).auDay

    inline operator fun times(velocity: Double) = (value * velocity).auDay

    inline operator fun times(velocity: Int) = (value * velocity).auDay

    inline operator fun div(velocity: Velocity) = value / velocity.value

    inline operator fun div(velocity: Double) = (value / velocity).auDay

    inline operator fun div(velocity: Int) = (value / velocity).auDay

    inline operator fun rem(velocity: Velocity) = (value % velocity.value).auDay

    inline operator fun rem(velocity: Double) = (value % velocity).auDay

    inline operator fun rem(velocity: Int) = (value % velocity).auDay

    inline operator fun unaryMinus() = (-value).auDay

    companion object {

        @JvmStatic val ZERO = Velocity(0.0)

        /**
         * Creates [Velocity] from au/day.
         */
        inline val Double.auDay
            get() = Velocity(this)

        /**
         * Creates [Velocity] from au/day.
         */
        inline val Int.auDay
            get() = Velocity(toDouble())

        /**
         * Creates [Velocity] from km/s.
         */
        inline val Double.kms
            get() = (this * DAYSEC / AU_KM).auDay

        /**
         * Creates [Velocity] from km/s.
         */
        inline val Int.kms
            get() = (this * DAYSEC / AU_KM).auDay

        /**
         * Creates [Velocity] from m/s.
         */
        inline val Double.ms
            get() = (this * DAYSEC / AU_M).auDay

        /**
         * Creates [Velocity] from m/s.
         */
        inline val Int.ms
            get() = (this * DAYSEC / AU_M).auDay
    }
}
