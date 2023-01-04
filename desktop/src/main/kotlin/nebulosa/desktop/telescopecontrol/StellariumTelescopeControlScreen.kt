package nebulosa.desktop.telescopecontrol

import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.indi.devices.mounts.Mount
import org.koin.core.component.inject

class StellariumTelescopeControlScreen(private val mount: Mount) : Screen("StellariumTelescopeControl", "nebulosa-stellarium") {

    private val telescopeControlManager by inject<TelescopeControlManager>()

    private val isConnected = SimpleBooleanProperty()
    private val isConnecting = SimpleBooleanProperty()

    @FXML private lateinit var host: TextField
    @FXML private lateinit var port: TextField
    @FXML private lateinit var connectAtStartup: CheckBox
    @FXML private lateinit var connect: Button

    override fun onCreate() {
        host.disableProperty().bind(isConnecting.or(isConnected))
        port.disableProperty().bind(host.disableProperty())
        connectAtStartup.disableProperty().bind(host.disableProperty())
        connect.disableProperty().bind(isConnecting)

        isConnected.addListener { _, _, value -> connect.graphic = if (value) Icon.closeCircle() else Icon.connection() }

        host.text = preferences.string("stellariumTelescopeControl.equipment.${mount.name}.host")
        port.text = preferences.string("stellariumTelescopeControl.equipment.${mount.name}.port")
        connectAtStartup.isSelected = preferences.bool("stellariumTelescopeControl.equipment.${mount.name}.connectAtStartup")
        isConnected.value = mount in telescopeControlManager && !telescopeControlManager[mount]!!.isClosed
    }

    @FXML
    @Synchronized
    private fun connect() {
        val server = telescopeControlManager[mount]

        if (server == null || server.isClosed) {
            try {
                isConnecting.value = true
                telescopeControlManager.startTCP(mount, host.text.trim(), port.text.trim().toInt())
                isConnected.value = true
                preferences.string("stellariumTelescopeControl.equipment.${mount.name}.host", host.text.trim())
                preferences.string("stellariumTelescopeControl.equipment.${mount.name}.port", host.text.trim())
            } catch (e: Throwable) {
                showAlert(
                    "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                    "Connection failed"
                )
            } finally {
                isConnecting.value = false
            }
        } else {
            telescopeControlManager[mount]?.close()
            isConnected.value = false
        }
    }

    @FXML
    private fun toggleConnectAtStartup() {
        preferences.bool("stellariumTelescopeControl.equipment.${mount.name}.connectAtStartup", connectAtStartup.isSelected)
    }
}
