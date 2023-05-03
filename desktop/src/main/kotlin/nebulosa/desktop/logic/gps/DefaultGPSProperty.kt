package nebulosa.desktop.logic.gps

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSEvent
import java.time.OffsetDateTime

class DefaultGPSProperty : AbstractDeviceProperty<GPS>(), GPSProperty {

    override val longitudeProperty = SimpleDoubleProperty()
    override val latitudeProperty = SimpleDoubleProperty()
    override val elevationProperty = SimpleDoubleProperty()
    override val timeProperty = SimpleObjectProperty(OffsetDateTime.now())

    override fun onChanged(prev: GPS?, device: GPS) {
        longitudeProperty.set(device.longitude.degrees)
        latitudeProperty.set(device.latitude.degrees)
        elevationProperty.set(device.elevation.meters)
        timeProperty.set(device.time)
    }

    override fun onReset() {
        longitudeProperty.set(0.0)
        latitudeProperty.set(0.0)
        elevationProperty.set(0.0)
        timeProperty.set(OffsetDateTime.now())
    }

    override suspend fun onDeviceEvent(event: DeviceEvent<*>, device: GPS) {
        when (event) {
            is GPSEvent<*> -> onChanged(device, device)
        }
    }
}
