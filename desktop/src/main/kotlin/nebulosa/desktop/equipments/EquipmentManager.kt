package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.connections.Connected
import nebulosa.desktop.connections.Disconnected
import nebulosa.desktop.core.eventbus.EventBus
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraDetached
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EquipmentManager : KoinComponent, Consumer<Any> {

    private val eventBus by inject<EventBus>()

    @JvmField val connected = SimpleBooleanProperty(false)
    @JvmField val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    @JvmField val selectedCamera = CameraProperty()

    init {
        eventBus.subscribe(this)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraAttached -> attachedCameras.add(event.device)
            is CameraDetached -> attachedCameras.remove(event.device)
            is Connected -> connected.value = true
            is Disconnected -> connected.value = false
        }
    }
}
