package nebulosa.indi.devices.gps

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.AbstractDevice
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.TextVector
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class GPSBase(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(client, handler, name), GPS {

    override var hasGPS = false
    override var longitude = Angle.ZERO
    override var latitude = Angle.ZERO
    override var elevation = Distance.ZERO
    override var time = OffsetDateTime.MIN!!

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "GEOGRAPHIC_COORD" -> {
                        if (!hasGPS && message is DefNumberVector && message.isReadOnly) {
                            hasGPS = true

                            handler.fireOnEventReceived(GPSAttached(this))
                        }

                        latitude = message["LAT"]!!.value.deg
                        longitude = message["LAT"]!!.value.deg
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

                        time = OffsetDateTime.of(utcTime, ZoneOffset.ofTotalSeconds((utcOffset * 60.0).toInt()))

                        handler.fireOnEventReceived(GPSTimeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun close() {}

    override fun toString(): String {
        return "GPS(hasGPS=$hasGPS, longitude=$longitude, latitude=$latitude," +
                " elevation=$elevation, time=$time)"
    }
}
