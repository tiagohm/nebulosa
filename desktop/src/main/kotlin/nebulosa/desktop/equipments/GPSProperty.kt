package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.gps.GPS
import nebulosa.indi.devices.gps.GPSCoordinateChanged
import nebulosa.indi.devices.gps.GPSTimeChanged
import java.time.OffsetDateTime

class GPSProperty : DeviceProperty<GPS>() {

    @JvmField val longitude = SimpleDoubleProperty()
    @JvmField val latitude = SimpleDoubleProperty()
    @JvmField val elevation = SimpleDoubleProperty()
    @JvmField val time = SimpleObjectProperty(OffsetDateTime.now())

    override fun changed(value: GPS) {
        longitude.set(value.longitude.degrees)
        latitude.set(value.latitude.degrees)
        elevation.set(value.elevation.meters)
        time.set(value.time)
    }

    override fun reset() {
        longitude.set(0.0)
        latitude.set(0.0)
        elevation.set(0.0)
        time.set(OffsetDateTime.now())
    }

    override fun accept(event: DeviceEvent<GPS>) {
        val device = event.device!!

        when (event) {
            is GPSCoordinateChanged -> Platform.runLater {
                longitude.set(device.longitude.degrees)
                latitude.set(device.latitude.degrees)
                elevation.set(device.elevation.meters)
            }
            is GPSTimeChanged -> Platform.runLater { time.set(device.time) }
        }
    }
}
