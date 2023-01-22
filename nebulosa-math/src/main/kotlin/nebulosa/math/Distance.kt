package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.SPEED_OF_LIGHT

/**
 * Represents a distance [value] in AU.
 */
@JvmInline
@Suppress("FloatingPointLiteralPrecision", "NOTHING_TO_INLINE")
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

    inline operator fun plus(distance: Distance) = (value + distance.value).au

    inline operator fun plus(distance: Double) = (value + distance).au

    inline operator fun plus(distance: Int) = (value + distance).au

    inline operator fun minus(distance: Distance) = (value - distance.value).au

    inline operator fun minus(distance: Double) = (value - distance).au

    inline operator fun minus(distance: Int) = (value - distance).au

    inline operator fun times(distance: Double) = (value * distance).au

    inline operator fun times(distance: Int) = (value * distance).au

    inline operator fun div(distance: Distance) = value / distance.value

    inline operator fun div(distance: Double) = (value / distance).au

    inline operator fun div(distance: Int) = (value / distance).au

    inline operator fun rem(distance: Distance) = (value % distance.value).au

    inline operator fun rem(distance: Double) = (value % distance).au

    inline operator fun rem(distance: Int) = (value % distance).au

    inline operator fun unaryMinus() = (-value).au

    override fun compareTo(other: Distance) = value.compareTo(other.value)

    companion object {

        @JvmStatic val ZERO = Distance(0.0)
        @JvmStatic val ONE = Distance(1.0)
        @JvmStatic val PARSEC = 1.0.parsec
        @JvmStatic val GIGAPARSEC = 1000000000.0.parsec

        /**
         * Creates [Distance] from AU.
         */
        inline val Double.au get() = Distance(this)

        /**
         * Creates [Distance] from AU.
         */
        inline val Int.au get() = Distance(toDouble())

        /**
         * Creates [Distance] from meters.
         */
        inline val Double.m get() = (this / AU_M).au

        /**
         * Creates [Distance] from meters.
         */
        inline val Int.m get() = (this / AU_M).au

        /**
         * Creates [Distance] from kilometers.
         */
        inline val Double.km get() = (this / AU_KM).au

        /**
         * Creates [Distance] from kilometers.
         */
        inline val Int.km get() = (this / AU_KM).au

        /**
         * Creates [Distance] from light-years.
         */
        inline val Double.ly get() = (this * (SPEED_OF_LIGHT * 31557600 / AU_M)).au

        /**
         * Creates [Distance] from light-years.
         */
        inline val Int.ly get() = (this * (SPEED_OF_LIGHT * 31557600 / AU_M)).au

        /**
         * Creates [Distance] from parsecs.
         */
        inline val Double.parsec get() = (this * 206264.806245480309552772371736702884).au

        /**
         * Creates [Distance] from parsecs.
         */
        inline val Int.parsec get() = (this * 206264.806245480309552772371736702884).au
    }
}
