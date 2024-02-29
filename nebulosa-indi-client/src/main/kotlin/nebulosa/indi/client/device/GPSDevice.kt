package nebulosa.indi.client.device

import nebulosa.indi.client.INDIClient
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSCoordinateChanged
import nebulosa.indi.device.gps.GPSTimeChanged
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.TextVector
import nebulosa.math.deg
import nebulosa.math.m
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal open class GPSDevice(
    override val sender: INDIClient,
    override val name: String,
) : INDIDevice(), GPS {

    @Volatile final override var hasGPS = true
        private set
    @Volatile final override var longitude = 0.0
        private set
    @Volatile final override var latitude = 0.0
        private set
    @Volatile final override var elevation = 0.0
        private set
    @Volatile final override var dateTime = OffsetDateTime.MIN!!
        private set

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "GEOGRAPHIC_COORD" -> {
                        latitude = message["LAT"]!!.value.deg
                        longitude = message["LONG"]!!.value.deg
                        elevation = message["ELEV"]!!.value.m

                        sender.fireOnEventReceived(GPSCoordinateChanged(this))
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "TIME_UTC" -> {
                        val utcTime = GPS.extractTime(message["UTC"]!!.value) ?: return
                        val utcOffset = message["OFFSET"]!!.value.toDoubleOrNull() ?: 0.0

                        dateTime = OffsetDateTime.of(utcTime, ZoneOffset.ofTotalSeconds((utcOffset * 60.0).toInt()))

                        sender.fireOnEventReceived(GPSTimeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun close() = Unit

    override fun toString(): String {
        return "GPS(hasGPS=$hasGPS, longitude=$longitude, latitude=$latitude," +
            " elevation=$elevation, dateTime=$dateTime)"
    }
}
