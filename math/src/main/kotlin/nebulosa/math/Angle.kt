package nebulosa.math

import nebulosa.constants.*
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Represents an Angle [value] in radians.
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE")
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

    inline operator fun plus(angle: Angle) = (value + angle.value).rad

    inline operator fun plus(angle: Double) = (value + angle).rad

    inline operator fun plus(angle: Float) = this + angle.toDouble()

    inline operator fun plus(angle: Int) = (value + angle).rad

    inline operator fun minus(angle: Angle) = (value - angle.value).rad

    inline operator fun minus(angle: Double) = (value - angle).rad

    inline operator fun minus(angle: Float) = this - angle.toDouble()

    inline operator fun minus(angle: Int) = (value - angle).rad

    inline operator fun times(angle: Double) = (value * angle).rad

    inline operator fun times(angle: Float) = this * angle.toDouble()

    inline operator fun times(angle: Int) = (value * angle).rad

    inline operator fun div(angle: Angle) = value / angle.value

    inline operator fun div(angle: Double) = (value / angle).rad

    inline operator fun div(angle: Float) = this / angle.toDouble()

    inline operator fun div(angle: Int) = (value / angle).rad

    inline operator fun rem(angle: Angle) = (value % angle.value).rad

    inline operator fun rem(angle: Double) = (value % angle).rad

    inline operator fun rem(angle: Float) = this % angle.toDouble()

    inline operator fun rem(angle: Int) = (value % angle).rad

    inline operator fun unaryMinus() = (-value).rad

    override fun compareTo(other: Angle) = value.compareTo(other.value)

    companion object : ClosedRange<Angle>, Comparator<Angle> {

        @JvmStatic val ZERO = Angle(0.0)
        @JvmStatic val SEMICIRCLE = Angle(PI)
        @JvmStatic val CIRCLE = Angle(TAU)

        override val start = ZERO

        override val endInclusive = CIRCLE

        override fun compare(a: Angle?, b: Angle?) = compareValues(a?.value, b?.value)

        @JvmStatic private val PARSE_COORDINATES_FACTOR = doubleArrayOf(1.0, 60.0, 3600.0)
        @JvmStatic private val PARSE_COORDINATES_NOT_NUMBER_REGEX = Regex("[^\\-\\d.]+")

        @JvmStatic
        fun parseCoordinatesAsDouble(input: String): Double {
            val trimmedInput = input.trim()
            val decimalInput = trimmedInput.toDoubleOrNull()
            if (decimalInput != null) return decimalInput

            val tokenizer = StringTokenizer(trimmedInput, " \t\n\rhms°'\"")
            var res = 0.0
            var idx = 0
            var negative = false

            while (idx < 3 && tokenizer.hasMoreElements()) {
                val token = tokenizer.nextToken().replace(PARSE_COORDINATES_NOT_NUMBER_REGEX, "").trim()

                if (token.isEmpty()) continue

                if (idx == 0 && token == "-") {
                    negative = true
                    continue
                }

                val value = token.toDoubleOrNull() ?: continue

                if (idx == 0 && value < 0.0) {
                    negative = true
                }

                res += abs(value) / PARSE_COORDINATES_FACTOR[idx++]
            }

            return if (idx == 0) throw IllegalArgumentException("invalid coordinate: $input")
            else if (negative) -res
            else res
        }

        @JvmStatic
        fun formatHMS(angle: Angle, format: String = "%02dh %02dm %05.02fs"): String {
            val value = angle.hours
            val hours = value.toInt()
            val minutes = (value - hours) * 60.0
            val seconds = (minutes - minutes.toInt()) * 60.0
            return format.format(hours, minutes.toInt(), seconds)
        }

        @JvmStatic
        fun formatDMS(angle: Angle, format: String = "%s%02d° %02d' %05.02f\""): String {
            val value = angle.degrees
            val degrees = abs(value)
            val minutes = (degrees - degrees.toInt()) * 60.0
            val seconds = (minutes - minutes.toInt()) * 60.0
            val sign = if (value < 0.0) "-" else "+"
            return format.format(sign, degrees.toInt(), minutes.toInt(), seconds)
        }

        /**
         * Creates [Angle] from radians.
         */
        inline val Double.rad get() = Angle(this)

        /**
         * Creates [Angle] from radians.
         */
        inline val Float.rad get() = Angle(toDouble())

        /**
         * Creates [Angle] from radians.
         */
        inline val Int.rad get() = Angle(toDouble())

        /**
         * Creates [Angle] from milliarcseconds.
         */
        inline val Double.mas get() = (this * MILLIASEC2RAD).rad

        /**
         * Creates [Angle] from milliarcseconds.
         */
        inline val Int.mas get() = (this * MILLIASEC2RAD).rad

        /**
         * Creates [Angle] from arcseconds.
         */
        inline val Double.arcsec get() = (this * ASEC2RAD).rad

        /**
         * Creates [Angle] from arcseconds.
         */
        inline val Int.arcsec get() = (this * ASEC2RAD).rad

        /**
         * Creates [Angle] from arcminutes.
         */
        inline val Double.arcmin get() = (this * AMIN2RAD).rad

        /**
         * Creates [Angle] from arcminutes.
         */
        inline val Int.arcmin get() = (this * AMIN2RAD).rad

        /**
         * Creates [Angle] from degrees.
         */
        inline val Double.deg get() = (this * DEG2RAD).rad

        /**
         * Creates [Angle] from degrees.
         */
        inline val Int.deg get() = (this * DEG2RAD).rad

        /**
         * Creates [Angle] from hours.
         */
        inline val Double.hours get() = (this * PI / 12.0).rad

        /**
         * Creates [Angle] from hours.
         */
        inline val Int.hours get() = (this * PI / 12.0).rad

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

