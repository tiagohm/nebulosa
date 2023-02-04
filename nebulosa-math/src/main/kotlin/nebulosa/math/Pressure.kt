package nebulosa.math

import nebulosa.math.Temperature.Companion.celsius
import kotlin.math.exp
import kotlin.math.pow

/**
 * Represents a pressure [value] in millibars.
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Pressure(val value: Double) {

    /**
     * Converts this pressure to pascal.
     */
    inline val pascal
        get() = value * 100.0

    /**
     * Converts this pressure to atmosphere.
     */
    inline val atm
        get() = value / 1013.25

    inline operator fun plus(pressure: Pressure) = (value + pressure.value).mbar

    inline operator fun plus(pressure: Double) = (value + pressure).mbar

    inline operator fun plus(pressure: Int) = (value + pressure).mbar

    inline operator fun minus(pressure: Pressure) = (value - pressure.value).mbar

    inline operator fun minus(pressure: Double) = (value - pressure).mbar

    inline operator fun minus(pressure: Int) = (value - pressure).mbar

    inline operator fun times(pressure: Pressure) = (value * pressure.value).mbar

    inline operator fun times(pressure: Double) = (value * pressure).mbar

    inline operator fun times(pressure: Int) = (value * pressure).mbar

    inline operator fun div(pressure: Pressure) = value / pressure.value

    inline operator fun div(pressure: Double) = (value / pressure).mbar

    inline operator fun div(pressure: Int) = (value / pressure).mbar

    inline operator fun rem(pressure: Pressure) = (value % pressure.value).mbar

    inline operator fun rem(pressure: Double) = (value % pressure).mbar

    inline operator fun rem(pressure: Int) = (value % pressure).mbar

    inline operator fun unaryMinus() = (-value).mbar

    companion object {

        @JvmStatic val ZERO = Pressure(0.0)

        /**
         * Creates [Pressure] from millibar.
         */
        inline val Double.mbar
            get() = Pressure(this)

        /**
         * Creates [Pressure] from millibar.
         */
        inline val Int.mbar
            get() = Pressure(toDouble())

        /**
         * Creates [Pressure] from pascal.
         */
        inline val Double.pascal
            get() = (this / 100.0).mbar

        /**
         * Creates [Pressure] from pascal.
         */
        inline val Int.pascal
            get() = (this / 100.0).mbar

        /**
         * Creates [Pressure] from atmosphere.
         */
        inline val Double.atm
            get() = (this * 1013.25).mbar

        /**
         * Creates [Pressure] from atmosphere.
         */
        inline val Int.atm
            get() = (this * 1013.25).mbar

        /**
         * Converts this distance (altitude) to pressure at specified [temperature].
         *
         * https://www.mide.com/air-pressure-at-altitude-calculator
         */
        @JvmStatic
        fun Distance.pressure(temperature: Temperature = 10.0.celsius): Pressure {
            val e = (9.80665 * 0.0289644) / (8.31432 * -0.0065)
            val k = temperature.kelvin

            return if (meters < 11000) {
                (1013.25 * (k / (k + (-0.0065 * meters))).pow(e)).mbar
            } else {
                val a = 1013.25 * (k / (k + (-0.0065 * 11000.0))).pow(e)
                val c = k + (11000 * -0.0065)
                (a * exp((-9.80665 * 0.0289644 * (meters - 11000.0)) / (8.31432 * c))).mbar
            }
        }
    }
}
