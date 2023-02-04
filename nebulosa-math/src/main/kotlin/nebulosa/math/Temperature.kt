package nebulosa.math

/**
 * Represents a temperature [value] in celsius.
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Temperature(val value: Double) {

    /**
     * Converts this temperature to fahrenheit.
     */
    inline val fahrenheit
        get() = value * 1.8 + 32

    /**
     * Converts this temperature to kelvin.
     */
    inline val kelvin
        get() = value + 273.15

    inline operator fun plus(temperature: Temperature) = (value + temperature.value).celsius

    inline operator fun plus(temperature: Double) = (value + temperature).celsius

    inline operator fun plus(temperature: Int) = (value + temperature).celsius

    inline operator fun minus(temperature: Temperature) = (value - temperature.value).celsius

    inline operator fun minus(temperature: Double) = (value - temperature).celsius

    inline operator fun minus(temperature: Int) = (value - temperature).celsius

    inline operator fun times(temperature: Temperature) = (value * temperature.value).celsius

    inline operator fun times(temperature: Double) = (value * temperature).celsius

    inline operator fun times(temperature: Int) = (value * temperature).celsius

    inline operator fun div(temperature: Temperature) = value / temperature.value

    inline operator fun div(temperature: Double) = (value / temperature).celsius

    inline operator fun div(temperature: Int) = (value / temperature).celsius

    inline operator fun rem(temperature: Temperature) = (value % temperature.value).celsius

    inline operator fun rem(temperature: Double) = (value % temperature).celsius

    inline operator fun rem(temperature: Int) = (value % temperature).celsius

    inline operator fun unaryMinus() = (-value).celsius

    companion object {

        @JvmStatic val ZERO = Temperature(0.0)

        /**
         * Creates [Temperature] from celsius.
         */
        inline val Double.celsius
            get() = Temperature(this)

        /**
         * Creates [Temperature] from celsius.
         */
        inline val Int.celsius
            get() = Temperature(toDouble())

        /**
         * Creates [Temperature] from fahrenheit.
         */
        inline val Double.fahrenheit
            get() = ((this - 32.0) / 1.8).celsius

        /**
         * Creates [Temperature] from fahrenheit.
         */
        inline val Int.fahrenheit
            get() = ((this - 32.0) / 1.8).celsius

        /**
         * Creates [Temperature] from kelvin.
         */
        inline val Double.kelvin
            get() = (this - 273.15).celsius

        /**
         * Creates [Temperature] from kelvin.
         */
        inline val Int.kelvin
            get() = (this - 273.15).celsius
    }
}
