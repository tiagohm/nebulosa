package nebulosa.desktop.cameras

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import nebulosa.desktop.home.CamerasShouldBeListed
import nebulosa.desktop.home.CamerasWereListed
import nebulosa.desktop.internal.Icon
import nebulosa.desktop.internal.Window
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.cameras.Camera

class CameraManager : Window("CameraManager") {

    @FXML private lateinit var cameras: ChoiceBox<Camera>
    @FXML private lateinit var connect: Button

    private val connected = SimpleBooleanProperty(false)

    init {
        isResizable = false

        connect.setOnAction { connect() }
        connect.disableProperty().bind(cameras.selectionModel.selectedItemProperty().isNull)

        connected.addListener { _, _, value -> connect.graphic = if (value) Icon.closeCircle() else Icon.connection() }
        cameras.selectionModel.selectedItemProperty().addListener { _, _, value -> connected.set(value?.isConnected ?: false) }
    }

    private val selectedCamera: Camera? get() = cameras.selectionModel.selectedItem

    override fun onStart() {
        eventBus.post(CamerasShouldBeListed)
    }

    override fun onEventReceived(event: Any) {
        when (event) {
            is CamerasWereListed -> {
                cameras.items.clear()
                cameras.items.addAll(event.cameras)

                if (selectedCamera !in event.cameras) {
                    cameras.selectionModel.select(null)
                }

                connected.set(selectedCamera?.isConnected == true)
            }
            is DeviceConnected -> {
                if (event.device === selectedCamera) {
                    Platform.runLater { connected.set(true) }
                }
            }
            is DeviceDisconnected -> {
                if (event.device === selectedCamera) {
                    Platform.runLater { connected.set(false) }
                }
            }
        }
    }

    @Synchronized
    private fun connect() {
        if (!connected.get()) {
            selectedCamera!!.connect()
        } else {
            selectedCamera!!.disconnect()
        }
    }
}
