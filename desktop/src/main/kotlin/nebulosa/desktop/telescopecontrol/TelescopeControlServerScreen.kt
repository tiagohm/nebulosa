package nebulosa.desktop.telescopecontrol

import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.toggle
import nebulosa.indi.devices.mounts.Mount
import org.koin.core.component.inject

class TelescopeControlServerScreen(private val mount: Mount) : Screen("TelescopeControlServer", "nebulosa-telescope-control") {

    private val telescopeControlServerManager by inject<TelescopeControlServerManager>()

    private val isConnected = SimpleBooleanProperty()
    private val isConnecting = SimpleBooleanProperty()

    @FXML private lateinit var types: ChoiceBox<TelescopeControlServerType>
    @FXML private lateinit var host: TextField
    @FXML private lateinit var port: TextField
    @FXML private lateinit var connect: Button

    init {
        title = "Telescope Control Server"
        isResizable = false
    }

    override fun onCreate() {
        host.disableProperty().bind(isConnecting or isConnected)

        port.disableProperty().bind(host.disableProperty())

        connect.disableProperty().bind(isConnecting)
        connect.textProperty().bind(isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        isConnected.on { connect.styleClass.toggle("text-red-700", "text-blue-grey-700") }

        types.valueProperty().on {
            it ?: return@on
            isConnected.set(!telescopeControlServerManager.isClosed(mount, it.type))
            val server = telescopeControlServerManager.get(mount, it.type) as? TelescopeControlTCPServer ?: return@on
            host.text = server.host
            port.text = "${server.port}"
        }

        types.value = TelescopeControlServerType.STELLARIUM
    }

    @FXML
    private fun connect() {
        val type = types.value ?: return

        try {
            val host = host.text.trim().ifBlank { "0.0.0.0" }
            val port = port.text.trim().toInt()

            isConnecting.set(true)

            if (type == TelescopeControlServerType.STELLARIUM) {
                if (telescopeControlServerManager.isClosed<TelescopeControlStellariumServer>(mount)) {
                    telescopeControlServerManager.startStellarium(mount, host, port)
                    isConnected.set(true)
                } else {
                    telescopeControlServerManager.stop<TelescopeControlStellariumServer>(mount)
                    isConnected.set(false)
                }
            } else if (type == TelescopeControlServerType.LX200) {
                if (telescopeControlServerManager.isClosed<TelescopeControlLX200Server>(mount)) {
                    telescopeControlServerManager.startLX200(mount, host, port)
                    isConnected.set(true)
                } else {
                    telescopeControlServerManager.stop<TelescopeControlLX200Server>(mount)
                    isConnected.set(false)
                }
            }
        } catch (e: Throwable) {
            showAlert(
                "A connection could not be established. Check your connection or server configuration.",
                "Connection failed"
            )
        } finally {
            isConnecting.set(false)
        }
    }
}
