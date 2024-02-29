@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nebulosa.math

import kotlin.math.exp
import kotlin.math.pow

/**
 * Represents a pressure value in millibars/hPa.
 */
typealias Pressure = Double

const val ONE_ATM: Pressure = 1013.25

inline val Pressure.toPascal
    get() = this * 100.0

inline val Pressure.toAtm
    get() = this / ONE_ATM

inline fun Millibar(value: Double): Pressure = value

inline fun Pascal(value: Double): Pressure = value.pascal

inline fun ATM(value: Double): Pressure = value.atm

inline val Double.mbar
    get() = Millibar(this)

inline val Int.mbar
    get() = toDouble().mbar

inline val Double.pascal
    get() = (this / 100.0).mbar

inline val Int.pascal
    get() = toDouble().pascal

inline val Double.atm
    get() = (this * ONE_ATM).mbar

inline val Int.atm
    get() = toDouble().atm

/**
 * Converts this distance (altitude) to pressure at specified [temperature].
 *
 * https://www.mide.com/air-pressure-at-altitude-calculator
 */
fun Distance.pressure(temperature: Temperature = 15.0.celsius): Pressure {
    val e = 9.80665 * 0.0289644 / (8.31432 * -0.0065)
    val k = temperature.toKelvin
    val m = toMeters

    return if (m < 11000) {
        (ONE_ATM * (k / (k - 0.0065 * m)).pow(e)).mbar
    } else {
        val a = ONE_ATM * (k / (k + (-0.0065 * 11000.0))).pow(e)
        val c = k + 11000 * -0.0065
        (a * exp(-9.80665 * 0.0289644 * (m - 11000.0) / (8.31432 * c))).mbar
    }
}
