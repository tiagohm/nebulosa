package nebulosa.math

/**
 * Represents a temperature [value] in celsius.
 */
@JvmInline
value class Temperature(val value: Double) : Comparable<Temperature> {

    /**
     * Converts this temperature to fahrenheit.
     */
    inline val fahrenheit get() = value * 1.8 + 32

    /**
     * Converts this temperature to kelvin.
     */
    inline val kelvin get() = value + 273.15

    operator fun plus(temperature: Temperature) = (value + temperature.value).celsius

    operator fun plus(temperature: Number) = (value + temperature.toDouble()).celsius

    operator fun minus(temperature: Temperature) = (value - temperature.value).celsius

    operator fun minus(temperature: Number) = (value - temperature.toDouble()).celsius

    operator fun times(temperature: Temperature) = (value * temperature.value).celsius

    operator fun times(temperature: Number) = (value * temperature.toDouble()).celsius

    operator fun div(temperature: Temperature) = value / temperature.value

    operator fun div(temperature: Number) = (value / temperature.toDouble()).celsius

    operator fun rem(temperature: Temperature) = (value % temperature.value).celsius

    operator fun rem(temperature: Number) = (value % temperature.toDouble()).celsius

    operator fun unaryMinus() = (-value).celsius

    override fun compareTo(other: Temperature) = value.compareTo(other.value)

    companion object {

        @JvmStatic val ZERO = Temperature(0.0)

        /**
         * Creates [Temperature] from celsius.
         */
        inline val Number.celsius get() = Temperature(toDouble())

        /**
         * Creates [Temperature] from fahrenheit.
         */
        inline val Number.fahrenheit get() = ((toDouble() - 32.0) / 1.8).celsius

        /**
         * Creates [Temperature] from kelvin.
         */
        inline val Number.kelvin get() = (toDouble() - 273.15).celsius
    }
}
