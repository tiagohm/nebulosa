package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.DAYSEC

/**
 * Represents a velocity [value] in au/day.
 */
@JvmInline
value class Velocity(val value: Double) : Comparable<Velocity> {

    /**
     * Converts this velocity to km/s.
     */
    inline val kms get() = value * AU_KM / DAYSEC

    /**
     * Converts this velocity to m/s.
     */
    inline val ms get() = value * AU_M / DAYSEC

    operator fun plus(velocity: Velocity) = (value + velocity.value).auDay

    operator fun plus(velocity: Number) = (value + velocity.toDouble()).auDay

    operator fun minus(velocity: Velocity) = (value - velocity.value).auDay

    operator fun minus(velocity: Number) = (value - velocity.toDouble()).auDay

    operator fun times(velocity: Velocity) = (value * velocity.value).auDay

    operator fun times(velocity: Number) = (value * velocity.toDouble()).auDay

    operator fun div(velocity: Velocity) = value / velocity.value

    operator fun div(velocity: Number) = (value / velocity.toDouble()).auDay

    operator fun rem(velocity: Velocity) = (value % velocity.value).auDay

    operator fun rem(velocity: Number) = (value % velocity.toDouble()).auDay

    operator fun unaryMinus() = (-value).auDay

    override fun compareTo(other: Velocity) = value.compareTo(other.value)

    companion object : Comparator<Velocity> {

        @JvmStatic val ZERO = Velocity(0.0)

        /**
         * Creates [Velocity] from au/day.
         */
        inline val Number.auDay get() = Velocity(toDouble())

        /**
         * Creates [Velocity] from km/s.
         */
        inline val Number.kms get() = (toDouble() * DAYSEC / AU_KM).auDay

        /**
         * Creates [Velocity] from m/s.
         */
        inline val Number.ms get() = (toDouble() * DAYSEC / AU_M).auDay

        override fun compare(a: Velocity?, b: Velocity?) = compareValues(a, b)
    }
}
