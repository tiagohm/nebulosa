package nebulosa.math

import nebulosa.constants.*
import nebulosa.constants.PI
import java.util.*
import kotlin.math.*

/**
 * Represents an Angle [value] in radians.
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Angle(val value: Double) {

    /**
     * Converts this angle to degrees.
     */
    inline val degrees
        get() = value * RAD2DEG

    /**
     * Converts this angle to hours.
     */
    inline val hours
        get() = value * (12.0 / PI)

    /**
     * Converts this angle to arcminutes.
     */
    inline val arcmin
        get() = value * (60.0 * 180.0 / PI)

    /**
     * Converts this angle to arcseconds.
     */
    inline val arcsec
        get() = value * (3600.0 * 180.0 / PI)

    /**
     * Converts this angle to milliarcseconds.
     */
    inline val mas
        get() = value * (3600000.0 * 180.0 / PI)

    /**
     * Returns the normalized angle.
     */
    inline val normalized
        get() = (value pmod TAU).rad

    /**
     * Gets the cosine of this angle.
     */
    inline val cos
        get() = cos(value)

    /**
     * Gets the sine of this angle.
     */
    inline val sin
        get() = sin(value)

    /**
     * Gets the tangent of this angle.
     */
    inline val tan
        get() = tan(value)

    inline val valid
        get() = value.isFinite()

    fun hms(): DoubleArray {
        val hours = normalized.hours
        val minutes = (hours - hours.toInt()) * 60.0 % 60.0
        val seconds = (minutes - minutes.toInt()) * 60.0 % 60.0
        return doubleArrayOf(hours, minutes, seconds)
    }

    fun dms(): DoubleArray {
        val degrees = abs(degrees)
        val minutes = (degrees - degrees.toInt()) * 60.0 % 60.0
        val seconds = (minutes - minutes.toInt()) * 60.0 % 60.0
        return doubleArrayOf(if (value < 0.0) -degrees else degrees, minutes, seconds)
    }

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

    fun format(formatter: AngleFormatter) = formatter.format(this)

    companion object {

        @JvmStatic val ZERO = Angle(0.0)
        @JvmStatic val SEMICIRCLE = Angle(PI)
        @JvmStatic val CIRCLE = Angle(TAU)
        @JvmStatic val QUARTER = Angle(PIOVERTWO)
        @JvmStatic val NaN = Angle(Double.NaN)

        @JvmStatic private val PARSE_COORDINATES_NOT_NUMBER_REGEX = Regex("[^\\-\\d.]+")

        @JvmStatic
        fun from(
            input: String?,
            isHours: Boolean = false,
            decimalIsHours: Boolean = isHours,
        ): Angle? {
            val trimmedInput = input?.trim() ?: return null

            val decimalInput = trimmedInput.toDoubleOrNull()
            if (decimalInput != null) return if (decimalIsHours) decimalInput.hours
            else decimalInput.deg

            val tokenizer = StringTokenizer(trimmedInput, " \t\n\rhms°'\":*#")
            val res = DoubleArray(3)
            var idx = 0
            var sign = 1.0

            while (idx < 3 && tokenizer.hasMoreElements()) {
                val token = tokenizer.nextToken().replace(PARSE_COORDINATES_NOT_NUMBER_REGEX, "").trim()

                if (token.isEmpty()) continue

                if (idx == 0 && token == "-") {
                    sign = -1.0
                    continue
                }

                val value = token.toDoubleOrNull() ?: continue

                if (idx == 0 && value < 0.0) {
                    sign = -1.0
                }

                res[idx++] = abs(value)
            }

            if (idx == 0) return null

            if (res[2] >= 60.0) {
                res[2] %= 60.0
                res[1] += 1.0
            }

            if (res[1] >= 60.0) {
                res[1] %= 60.0
                res[0] += 1.0
            }

            val value = sign * (res[0] + res[1] / 60.0 + res[2] / 3600.0)

            return if (isHours) value.hours else value.deg
        }

        /**
         * Creates [Angle] from radians.
         */
        inline val Double.rad
            get() = Angle(this)

        /**
         * Creates [Angle] from radians.
         */
        inline val Float.rad
            get() = Angle(toDouble())

        /**
         * Creates [Angle] from radians.
         */
        inline val Int.rad
            get() = Angle(toDouble())

        /**
         * Creates [Angle] from milliarcseconds.
         */
        inline val Double.mas
            get() = (this * MILLIASEC2RAD).rad

        /**
         * Creates [Angle] from milliarcseconds.
         */
        inline val Int.mas
            get() = (this * MILLIASEC2RAD).rad

        /**
         * Creates [Angle] from arcseconds.
         */
        inline val Double.arcsec
            get() = (this * ASEC2RAD).rad

        /**
         * Creates [Angle] from arcseconds.
         */
        inline val Int.arcsec
            get() = (this * ASEC2RAD).rad

        /**
         * Creates [Angle] from arcminutes.
         */
        inline val Double.arcmin
            get() = (this * AMIN2RAD).rad

        /**
         * Creates [Angle] from arcminutes.
         */
        inline val Int.arcmin
            get() = (this * AMIN2RAD).rad

        /**
         * Creates [Angle] from degrees.
         */
        inline val Double.deg
            get() = (this * DEG2RAD).rad

        /**
         * Creates [Angle] from degrees.
         */
        inline val Int.deg
            get() = (this * DEG2RAD).rad

        /**
         * Creates [Angle] from hours.
         */
        inline val Double.hours
            get() = (this * PI / 12.0).rad

        /**
         * Creates [Angle] from hours.
         */
        inline val Int.hours
            get() = (this * PI / 12.0).rad

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

