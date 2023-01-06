package nebulosa.desktop.telescopecontrol

import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.scene.Screen
import nebulosa.indi.devices.mounts.Mount
import org.koin.core.component.inject

class StellariumTelescopeControlScreen(private val mount: Mount) : Screen("StellariumTelescopeControl", "nebulosa-stellarium") {

    private val telescopeControlManager by inject<TelescopeControlManager>()

    private val isConnected = SimpleBooleanProperty()
    private val isConnecting = SimpleBooleanProperty()

    @FXML private lateinit var host: TextField
    @FXML private lateinit var port: TextField
    @FXML private lateinit var connect: Button

    init {
        title = "Stellarium Telescope Control"
        isResizable = false
    }

    override fun onCreate() {
        host.disableProperty().bind(isConnecting or isConnected)
        port.disableProperty().bind(host.disableProperty())
        connect.disableProperty().bind(isConnecting)

        connect.graphicProperty().bind(isConnected.between(Icon.closeCircle(), Icon.connection()))

        host.text = preferences.string("stellariumTelescopeControl.equipment.${mount.name}.host")
        port.text = preferences.string("stellariumTelescopeControl.equipment.${mount.name}.port")
        isConnected.set(mount in telescopeControlManager && !telescopeControlManager[mount]!!.isClosed)
    }

    @FXML
    private fun connect() {
        val server = telescopeControlManager[mount]

        if (server == null || server.isClosed) {
            try {
                isConnecting.set(true)

                val host = host.text.trim().ifBlank { "localhost" }
                val port = port.text.trim().toInt()

                telescopeControlManager.startTCP(mount, host, port)

                isConnected.set(true)

                preferences.string("stellariumTelescopeControl.equipment.${mount.name}.host", host)
                preferences.int("stellariumTelescopeControl.equipment.${mount.name}.port", port)
            } catch (e: Throwable) {
                showAlert(
                    "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                    "Connection failed"
                )
            } finally {
                isConnecting.set(false)
            }
        } else {
            telescopeControlManager[mount]?.close()
            isConnected.set(false)
        }
    }
}
