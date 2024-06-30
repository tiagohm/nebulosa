package nebulosa.horizons

import java.time.LocalDateTime
import java.time.ZoneOffset

data class HorizonsElement(val dateTime: LocalDateTime) : HashMap<HorizonsQuantity, String>(7), Comparable<HorizonsElement> {

    fun asStringOrNull(quantity: HorizonsQuantity, index: Int = 0): String? {
        return if (quantity.numberOfColumns > 1) this[quantity]?.split(',')?.get(index)
        else this[quantity]
    }

    fun asString(quantity: HorizonsQuantity, defaultValue: String = "", index: Int = 0): String {
        return if (quantity.numberOfColumns > 1) this[quantity]?.split(',')?.get(index) ?: defaultValue
        else this[quantity] ?: defaultValue
    }

    fun asDoubleOrNull(quantity: HorizonsQuantity, index: Int = 0): Double? {
        return asStringOrNull(quantity, index = index)?.toDoubleOrNull()
    }

    fun asDouble(quantity: HorizonsQuantity, defaultValue: Double = 0.0, index: Int = 0): Double {
        return asDoubleOrNull(quantity, index) ?: defaultValue
    }

    inline fun <reified T : Enum<T>> asEnum(quantity: HorizonsQuantity, defaultValue: T, index: Int = 0): T {
        return enumValueOf(asString(quantity, defaultValue.name, index).uppercase())
    }

    override fun compareTo(other: HorizonsElement): Int {
        return dateTime.compareTo(other.dateTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HorizonsElement) return false
        if (!super.equals(other)) return false
        return dateTime == other.dateTime
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + dateTime.hashCode()
        return result
    }

    override fun toString() = "HorizonsElement(time=$dateTime, quantities=${super.toString()})"

    companion object {

        @JvmStatic
        fun of(ephemeris: List<HorizonsElement>, dateTime: LocalDateTime): HorizonsElement? {
            val seconds = dateTime.toEpochSecond(ZoneOffset.UTC)
            return ephemeris.find { it.dateTime.toEpochSecond(ZoneOffset.UTC) >= seconds }
        }
    }
}
