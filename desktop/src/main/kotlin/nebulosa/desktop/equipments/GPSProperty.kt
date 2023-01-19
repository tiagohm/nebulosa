package nebulosa.desktop.equipments

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSCoordinateChanged
import nebulosa.indi.device.gps.GPSTimeChanged
import java.time.OffsetDateTime

class GPSProperty : DeviceProperty<GPS>() {

    @JvmField val longitude = SimpleDoubleProperty()
    @JvmField val latitude = SimpleDoubleProperty()
    @JvmField val elevation = SimpleDoubleProperty()
    @JvmField val time = SimpleObjectProperty(OffsetDateTime.now())

    override fun changed(prev: GPS?, new: GPS) {
        longitude.set(new.longitude.degrees)
        latitude.set(new.latitude.degrees)
        elevation.set(new.elevation.meters)
        time.set(new.time)
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
            is GPSCoordinateChanged -> {
                longitude.set(device.longitude.degrees)
                latitude.set(device.latitude.degrees)
                elevation.set(device.elevation.meters)
            }
            is GPSTimeChanged -> time.set(device.time)
        }
    }
}
