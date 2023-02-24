package nebulosa.desktop.gui.telescopecontrol

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.CLOSE_CIRCLE_ICON
import nebulosa.desktop.gui.CONNECTION_ICON
import nebulosa.desktop.logic.mount.MountManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlManager
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlServerType
import nebulosa.desktop.logic.toggle
import nebulosa.desktop.view.telescopecontrol.TelescopeControlView
import org.slf4j.LoggerFactory

class TelescopeControlWindow(private val mountManager: MountManager) : AbstractWindow("TelescopeControl", "nebulosa-telescope-control"),
    TelescopeControlView {

    @FXML private lateinit var serverTypeChoiceBox: ChoiceBox<TelescopeControlServerType>
    @FXML private lateinit var hostTextField: TextField
    @FXML private lateinit var portTextField: TextField
    @FXML private lateinit var connectButton: Button

    private val telescopeControlManager = TelescopeControlManager(this)

    init {
        title = "Telescope Control"
        resizable = false
    }

    override fun onCreate() {
        serverTypeChoiceBox.value = TelescopeControlServerType.STELLARIUM
        serverTypeChoiceBox.valueProperty().on { telescopeControlManager.updateConnectionStatus() }
    }

    override fun onStart() {
        telescopeControlManager.updateConnectionStatus()
    }

    override val type
        get() = serverTypeChoiceBox.value!!

    override val host
        get() = hostTextField.text.trim().ifBlank { "0.0.0.0" }

    override val port
        get() = portTextField.text.trim().toIntOrNull() ?: -1

    override fun updateConnectionStatus(connected: Boolean, host: String, port: Int) {
        connectButton.text = if (connected) CLOSE_CIRCLE_ICON else CONNECTION_ICON
        connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700", connected)

        hostTextField.text = host
        portTextField.text = if (port > 0) "$port" else ""
    }

    @FXML
    @Synchronized
    private fun connect() {
        try {
            telescopeControlManager.connect()
        } catch (e: Throwable) {
            LOG.error("connection error", e)

            showAlert(
                "A connection could not be established. Check your connection or server configuration.",
                "Connection failed"
            )
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TelescopeControlWindow::class.java)
    }
}
