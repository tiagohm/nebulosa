package nebulosa.indi.device.gps

import nebulosa.indi.device.Device
import nebulosa.nova.position.GeographicCoordinate
import nebulosa.time.TimeStampedWithOffset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface GPS : GeographicCoordinate, TimeStampedWithOffset, Device {

    val hasGPS: Boolean

    companion object {

        @JvmStatic val UTC_TIME_FORMAT_1 = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss")!!
        @JvmStatic val UTC_TIME_FORMAT_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")!!

        @JvmStatic
        fun extractTime(input: String): LocalDateTime? {
            val trimmedImput = input.trim()

            return runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_1) }.getOrNull()
                ?: runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_2) }.getOrNull()
        }

        @JvmStatic
        fun formatTime(dateTime: LocalDateTime): String {
            return dateTime.format(UTC_TIME_FORMAT_1)
        }

        @JvmStatic val DRIVERS = setOf(
            "indi_gpsd",
            "indi_gpsnmea",
            "indi_simulator_gps",
        )
    }
}
