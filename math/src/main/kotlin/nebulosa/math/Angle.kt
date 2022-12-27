package nebulosa.math

import nebulosa.constants.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Represents an Angle [value] in radians.
 */
@JvmInline
value class Angle(val value: Double) : Comparable<Angle> {

    /**
     * Converts this angle to degrees.
     */
    inline val degrees get() = value * RAD2DEG

    /**
     * Converts this angle to hours.
     */
    inline val hours get() = value * (12.0 / PI)

    /**
     * Converts this angle to arcminutes.
     */
    inline val arcmin get() = value * (60.0 * 180.0 / PI)

    /**
     * Converts this angle to arcseconds.
     */
    inline val arcsec get() = value * (3600.0 * 180.0 / PI)

    /**
     * Converts this angle to milliarcseconds.
     */
    inline val mas get() = value * (3600000.0 * 180.0 / PI)

    /**
     * Returns the normalized angle.
     */
    inline val normalized get() = (value pmod TAU).rad

    /**
     * Gets the cosine of this angle.
     */
    inline val cos get() = cos(value)

    /**
     * Gets the sine of this angle.
     */
    inline val sin get() = sin(value)

    /**
     * Gets the tangent of this angle.
     */
    inline val tan get() = tan(value)

    operator fun plus(angle: Angle) = (value + angle.value).rad

    operator fun plus(angle: Number) = (value + angle.toDouble()).rad

    operator fun minus(angle: Angle) = (value - angle.value).rad

    operator fun minus(angle: Number) = (value - angle.toDouble()).rad

    operator fun times(angle: Number) = (value * angle.toDouble()).rad

    operator fun div(angle: Angle) = value / angle.value

    operator fun div(angle: Number) = (value / angle.toDouble()).rad

    operator fun rem(angle: Angle) = (value % angle.value).rad

    operator fun rem(angle: Number) = (value % angle.toDouble()).rad

    operator fun unaryMinus() = (-value).rad

    override fun compareTo(other: Angle) = value.compareTo(other.value)

    companion object : ClosedRange<Angle>, Comparator<Angle> {

        @JvmStatic val ZERO = Angle(0.0)

        @JvmStatic val SEMICIRCLE = Angle(PI)

        @JvmStatic val CIRCLE = Angle(TAU)

        override val start = ZERO

        override val endInclusive = CIRCLE

        override fun compare(a: Angle?, b: Angle?) = compareValues(a?.value, b?.value)

        /**
         * Creates [Angle] from radians.
         */
        inline val Number.rad get() = Angle(toDouble())

        /**
         * Creates [Angle] from milliarcseconds.
         */
        inline val Number.mas get() = (toDouble() * MILLIASEC2RAD).rad

        /**
         * Creates [Angle] from arcseconds.
         */
        inline val Number.arcsec get() = (toDouble() * ASEC2RAD).rad

        /**
         * Creates [Angle] from arcminutes.
         */
        inline val Number.arcmin get() = (toDouble() * AMIN2RAD).rad

        /**
         * Creates [Angle] from degrees.
         */
        inline val Number.deg get() = (toDouble() * DEG2RAD).rad

        /**
         * Creates [Angle] from hours.
         */
        inline val Number.hours get() = (toDouble() * PI / 12.0).rad

        /**
         * Creates the [Angle] from [hour], [minute] and [second].
         */
        @JvmStatic
        fun hms(
            hour: Int,
            minute: Int,
            second: Double = 0.0,
        ) = (hour + minute / 60.0 + second / 3600.0).hours

        /**
         * Creates the [Angle] from [degrees], [minute] and [second].
         */
        @JvmStatic
        fun dms(
            degrees: Int,
            minute: Int,
            second: Double = 0.0,
            negative: Boolean = degrees < 0,
        ) = (abs(degrees) + minute / 60.0 + second / 3600.0).let { if (negative) -it else it }.deg
    }
}

