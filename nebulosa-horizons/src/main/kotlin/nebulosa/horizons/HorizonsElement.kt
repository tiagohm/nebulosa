package nebulosa.horizons

import java.time.LocalDateTime
import java.time.ZoneOffset

class HorizonsElement(val time: LocalDateTime) : HashMap<HorizonsQuantity, String>(7) {

    val utcMinutes = time.toEpochSecond(ZoneOffset.UTC) / 60L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HorizonsElement) return false
        if (!super.equals(other)) return false

        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }

    override fun toString(): String {
        return "HorizonsElement(time=$time, quantities=${super.toString()})"
    }
}
