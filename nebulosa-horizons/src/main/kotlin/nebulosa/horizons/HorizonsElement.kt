package nebulosa.horizons

import java.time.LocalDateTime
import java.time.ZoneOffset

data class HorizonsElement(val time: LocalDateTime) : HashMap<HorizonsQuantity, String>(7), Comparable<HorizonsElement> {

    fun asDouble(quantity: HorizonsQuantity, defaultValue: Double = 0.0): Double {
        return this[quantity]?.toDoubleOrNull() ?: defaultValue
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
