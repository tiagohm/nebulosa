@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nebulosa.math

/**
 * Represents a temperature value in celsius.
 */
typealias Temperature = Double

inline val Temperature.toFahrenheit
    get() = this * 1.8 + 32

inline val Temperature.toKelvin
    get() = this + 273.15

inline fun Celsius(value: Double): Temperature = value

inline fun Fahrenheit(value: Double) = value.fahrenheit

inline fun Kelvin(value: Double) = value.kelvin

inline val Double.celsius
    get() = Celsius(this)

inline val Int.celsius
    get() = toDouble().celsius

inline val Double.fahrenheit
    get() = ((this - 32.0) / 1.8).celsius

inline val Int.fahrenheit
    get() = toDouble().fahrenheit

inline val Double.kelvin
    get() = (this - 273.15).celsius

inline val Int.kelvin
    get() = toDouble().kelvin
