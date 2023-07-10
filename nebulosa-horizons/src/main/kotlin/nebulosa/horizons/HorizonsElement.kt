package nebulosa.horizons

import java.time.LocalDateTime
import java.time.ZoneOffset

data class HorizonsElement(val time: LocalDateTime) : HashMap<HorizonsQuantity, String>(7), Comparable<HorizonsElement> {

    fun asString(quantity: HorizonsQuantity, defaultValue: String = "", index: Int = 0): String {
        return if (quantity.numberOfColumns > 1) this[quantity]?.split(',')?.get(index) ?: defaultValue
        else this[quantity] ?: defaultValue
    }

    fun asDouble(quantity: HorizonsQuantity, defaultValue: Double = 0.0, index: Int = 0): Double {
        return asString(quantity, index = index).toDoubleOrNull() ?: defaultValue
    }

    inline fun <reified T : Enum<T>> asEnum(quantity: HorizonsQuantity, defaultValue: T, index: Int = 0): T {
        return enumValueOf(asString(quantity, defaultValue.name, index).uppercase())
    }

    override fun compareTo(other: HorizonsElement): Int {
        return time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HorizonsElement) return false
        if (!super.equals(other)) return false
        return time == other.time
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }

    override fun toString() = "HorizonsElement(time=$time, quantities=${super.toString()})"

    companion object {

        @JvmStatic
        fun of(ephemeris: List<HorizonsElement>, dateTime: LocalDateTime): HorizonsElement? {
            val seconds = dateTime.toEpochSecond(ZoneOffset.UTC)
            return ephemeris.find { it.time.toEpochSecond(ZoneOffset.UTC) >= seconds }
        }
    }
}
