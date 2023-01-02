package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.SPEED_OF_LIGHT

/**
 * Represents a distance [value] in AU.
 */
@JvmInline
value class Distance(val value: Double) : Comparable<Distance> {

    /**
     * Converts this distance to meters.
     */
    inline val meters get() = value * AU_M

    /**
     * Converts this distance to kilometers.
     */
    inline val kilometers get() = value * AU_KM

    /**
     * Converts this distance to light-years.
     */
    inline val lightYears get() = value / (SPEED_OF_LIGHT * 31557600 / AU_M)

    operator fun plus(temperature: Distance) = (value + temperature.value).au

    operator fun plus(temperature: Number) = (value + temperature.toDouble()).au

    operator fun minus(temperature: Distance) = (value - temperature.value).au

    operator fun minus(temperature: Number) = (value - temperature.toDouble()).au

    operator fun times(temperature: Number) = (value * temperature.toDouble()).au

    operator fun div(temperature: Distance) = value / temperature.value

    operator fun div(temperature: Number) = (value / temperature.toDouble()).au

    operator fun rem(temperature: Distance) = (value % temperature.value).au

    operator fun rem(temperature: Number) = (value % temperature.toDouble()).au

    operator fun unaryMinus() = (-value).au

    override fun compareTo(other: Distance) = value.compareTo(other.value)

    companion object {

        @JvmStatic val ZERO = Distance(0.0)

        @JvmStatic val ONE = Distance(1.0)

        /**
         * Creates [Distance] from AU.
         */
        inline val Number.au get() = Distance(toDouble())

        /**
         * Creates [Distance] from meters.
         */
        inline val Number.m get() = (toDouble() / AU_M).au

        /**
         * Creates [Distance] from kilometers.
         */
        inline val Number.km get() = (toDouble() / AU_KM).au

        /**
         * Creates [Distance] from light-years.
         */
        inline val Number.ly get() = (toDouble() * (SPEED_OF_LIGHT * 31557600 / AU_M)).au
    }
}
