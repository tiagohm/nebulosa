package nebulosa.indi.devices.gps

import nebulosa.indi.devices.Device
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

    val time: OffsetDateTime

    companion object {

        private val UTC_TIME_FORMAT_1 = DateTimeFormatter.ofPattern("yyyy/MM/ddTHH:mm:ss")
        private val UTC_TIME_FORMAT_2 = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss")

        @JvmStatic
        fun extractTime(input: String): LocalDateTime? {
            val trimmedImput = input.trim()

            return runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_1) }.getOrNull()
                ?: runCatching { LocalDateTime.parse(trimmedImput, UTC_TIME_FORMAT_2) }.getOrNull()
        }

        @JvmStatic val DRIVERS = setOf(
            "indi_celestron_gps",
            "indi_simulator_gps",
            "indi_lx200gps",
        )
    }
}
