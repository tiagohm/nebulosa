package nebulosa.desktop.logic.gps

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.gps.GPS
import java.time.OffsetDateTime

interface GPSProperty : DeviceProperty<GPS> {

    val longitudeProperty: SimpleDoubleProperty
    val latitudeProperty: SimpleDoubleProperty
    val elevationProperty: SimpleDoubleProperty
    val timeProperty: SimpleObjectProperty<OffsetDateTime>

    val longitude
        get() = longitudeProperty.get()

    val latitude
        get() = latitudeProperty.get()

    val elevation
        get() = elevationProperty.get()

    val time: OffsetDateTime?
        get() = timeProperty.get()
}
