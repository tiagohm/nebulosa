package nebulosa.indi.device.gps

import nebulosa.indi.device.Device
import nebulosa.nova.position.GeographicCoordinate
import nebulosa.time.TimeStampedWithOffset
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface GPS : GeographicCoordinate, TimeStampedWithOffset, Device {

    val hasGPS: Boolean

    companion object {

        @JvmStatic val ZERO_DATE_TIME = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)!!

        @JvmStatic val UTC_TIME_FORMAT_1 = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss")!!
        @JvmStatic val UTC_TIME_FORMAT_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")!!

        @JvmStatic
        fun parseTime(input: String): LocalDateTime? {
            val trimmedImput = input.trim()

            return runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_2) }.getOrNull()
                ?: runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_1) }.getOrNull()
        }

        @JvmStatic
        fun parseOffset(input: String): Int {
            val trimmedImput = input.trim()

            return if (':' in trimmedImput) {
                val (hour, minute) = trimmedImput.split(':')
                (hour.toIntOrNull()?.times(60) ?: 0) + (minute.toIntOrNull() ?: 0)
            } else {
                trimmedImput.toDoubleOrNull()?.times(60)?.toInt() ?: 0
            }
        }

        @JvmStatic
        fun formatTime(dateTime: LocalDateTime): String {
            return dateTime.format(UTC_TIME_FORMAT_2)
        }
    }
}
