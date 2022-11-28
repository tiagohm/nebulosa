package nebulosa.math

import nebulosa.math.Temperature.Companion.celsius
import kotlin.math.exp
import kotlin.math.pow

/**
 * Represents a pressure [value] in millibars.
 */
@JvmInline
value class Pressure(val value: Double) : Comparable<Pressure> {

    /**
     * Converts this pressure to pascal.
     */
    inline val pascal get() = value * 100.0

    /**
     * Converts this pressure to atmosphere.
     */
    inline val atm get() = value / 1013.25

    operator fun plus(pressure: Pressure) = (value + pressure.value).mbar

    operator fun plus(pressure: Number) = (value + pressure.toDouble()).mbar

    operator fun minus(pressure: Pressure) = (value - pressure.value).mbar

    operator fun minus(pressure: Number) = (value - pressure.toDouble()).mbar

    operator fun times(pressure: Pressure) = (value * pressure.value).mbar

    operator fun times(pressure: Number) = (value * pressure.toDouble()).mbar

    operator fun div(pressure: Pressure) = value / pressure.value

    operator fun div(pressure: Number) = (value / pressure.toDouble()).mbar

    operator fun rem(pressure: Pressure) = (value % pressure.value).mbar

    operator fun rem(pressure: Number) = (value % pressure.toDouble()).mbar

    operator fun unaryMinus() = (-value).mbar

    override fun compareTo(other: Pressure) = value.compareTo(other.value)

    companion object : Comparator<Pressure> {

        @JvmStatic val ZERO = Pressure(0.0)

        /**
         * Creates [Pressure] from millibar.
         */
        inline val Number.mbar get() = Pressure(toDouble())

        /**
         * Creates [Pressure] from pascal.
         */
        inline val Number.pascal get() = (toDouble() / 100.0).mbar

        /**
         * Creates [Pressure] from atmosphere.
         */
        inline val Number.atm get() = (toDouble() * 1013.25).mbar

        override fun compare(a: Pressure?, b: Pressure?) = compareValues(a, b)

        /**
         * Converts this distance (altitude) to pressure at specified [temperature].
         *
         * https://www.mide.com/air-pressure-at-altitude-calculator
         */
        @Suppress("LocalVariableName")
        @JvmStatic
        fun Distance.pressure(temperature: Temperature = 10.0.celsius): Pressure {
            val k = temperature.kelvin
            val M = 0.0289644
            val g = 9.80665
            val R = 8.31432
            val e = (g * M) / (8.31432 * -0.0065)

            return if (m < 11000) {
                (1013.25 * (k / (k + (-0.0065 * m))).pow(e)).mbar
            } else {
                val a = 1013.25 * (k / (k + (-0.0065 * 11000.0))).pow(e)
                val c = k + (11000 * -0.0065)
                (a * exp((-g * M * (m - 11000.0)) / (R * c))).mbar
            }
        }
    }
}
