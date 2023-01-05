package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.SPEED_OF_LIGHT

/**
 * Represents a distance [value] in AU.
 */
@JvmInline
@Suppress("FloatingPointLiteralPrecision")
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

    /**
     * Converts this distance to parsecs.
     */
    inline val parsecs get() = value / 206264.806245480309552772371736702884

    operator fun plus(distance: Distance) = (value + distance.value).au

    operator fun plus(distance: Number) = (value + distance.toDouble()).au

    operator fun minus(distance: Distance) = (value - distance.value).au

    operator fun minus(distance: Number) = (value - distance.toDouble()).au

    operator fun times(distance: Number) = (value * distance.toDouble()).au

    operator fun div(distance: Distance) = value / distance.value

    operator fun div(distance: Number) = (value / distance.toDouble()).au

    operator fun rem(distance: Distance) = (value % distance.value).au

    operator fun rem(distance: Number) = (value % distance.toDouble()).au

    operator fun unaryMinus() = (-value).au

    override fun compareTo(other: Distance) = value.compareTo(other.value)

    companion object {

        @JvmStatic val ZERO = Distance(0.0)
        @JvmStatic val ONE = Distance(1.0)
        @JvmStatic val PARSEC = 1.0.parsec
        @JvmStatic val GIGAPARSEC = 1000000000.0.parsec

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
         * Creates [Distance] from parsecs.
         */
        inline val Number.ly get() = (toDouble() * (SPEED_OF_LIGHT * 31557600 / AU_M)).au

        inline val Number.parsec get() = (toDouble() * 206264.806245480309552772371736702884).au
    }
}
