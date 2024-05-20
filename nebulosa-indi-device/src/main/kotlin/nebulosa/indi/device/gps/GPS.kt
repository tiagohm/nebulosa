package nebulosa.indi.device.gps

import nebulosa.indi.device.Device
import nebulosa.math.Angle
import nebulosa.math.Distance
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface GPS : Device {

    val hasGPS: Boolean

    val longitude: Angle

    val latitude: Angle

    val elevation: Distance

    val dateTime: OffsetDateTime

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
