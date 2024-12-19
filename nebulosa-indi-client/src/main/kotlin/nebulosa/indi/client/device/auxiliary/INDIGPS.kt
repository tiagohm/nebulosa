package nebulosa.indi.client.device.auxiliary

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSCoordinateChanged
import nebulosa.indi.device.gps.GPSTimeChanged
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.TextVector
import nebulosa.math.deg
import nebulosa.math.m

internal open class INDIGPS(
    final override val sender: INDIClient,
    final override val driver: INDIDriverInfo,
) : INDIDevice(), GPS {

    override val type
        get() = DeviceType.GPS

    @Volatile final override var hasGPS = true
    @Volatile final override var longitude = 0.0
    @Volatile final override var latitude = 0.0
    @Volatile final override var elevation = 0.0
    @Volatile final override var dateTime = GPS.ZERO_DATE_TIME
    @Volatile final override var offsetInSeconds = 0

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
                        dateTime = GPS.parseTime(message["UTC"]?.value ?: return) ?: return
                        offsetInSeconds = GPS.parseOffset(message["OFFSET"]?.value ?: return) * 60

                        sender.fireOnEventReceived(GPSTimeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun close() = Unit

    override fun toString() = "GPS(hasGPS=$hasGPS, longitude=$longitude, latitude=$latitude," +
            " elevation=$elevation, dateTime=$dateTime)"
}
