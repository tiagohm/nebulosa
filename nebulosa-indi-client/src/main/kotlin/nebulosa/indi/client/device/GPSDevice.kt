package nebulosa.indi.client.device

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

internal class GPSDevice(
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(handler, name), GPS {

    override val hasGPS = true
    override var longitude = 0.0
    override var latitude = 0.0
    override var elevation = 0.0
    override var dateTime = OffsetDateTime.MIN!!

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "GEOGRAPHIC_COORD" -> {
                        latitude = message["LAT"]!!.value.deg
                        longitude = message["LONG"]!!.value.deg
                        elevation = message["ELEV"]!!.value.m

                        handler.fireOnEventReceived(GPSCoordinateChanged(this))
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "TIME_UTC" -> {
                        val utcTime = GPS.extractTime(message["UTC"]!!.value) ?: return
                        val utcOffset = message["OFFSET"]!!.value.toDoubleOrNull() ?: 0.0

                        dateTime = OffsetDateTime.of(utcTime, ZoneOffset.ofTotalSeconds((utcOffset * 60.0).toInt()))

                        handler.fireOnEventReceived(GPSTimeChanged(this))
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
