package nebulosa.desktop.logic

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.indi.device.Device

interface DeviceProperty<D : Device> : Property<D>, DevicePropertyListener<D> {

    val connectedProperty: SimpleBooleanProperty
    val connectingProperty: SimpleBooleanProperty

    val connected
        get() = connectedProperty.get()

    val connecting
        get() = connectingProperty.get()

    fun registerListener(listener: DevicePropertyListener<D>)

    fun unregisterListener(listener: DevicePropertyListener<D>)

    fun connect() {
        if (value.isConnected) {
            value.disconnect()
        } else {
            value.connect()
        }
    }
}
