package nebulosa.desktop.gui.telescopecontrol

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlManager
import nebulosa.desktop.view.telescopecontrol.TelescopeControlType
import nebulosa.desktop.view.telescopecontrol.TelescopeControlView
import nebulosa.log.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class TelescopeControlWindow : AbstractWindow("TelescopeControl", "remote-control"), TelescopeControlView {

    @Lazy @Autowired private lateinit var telescopeControlManager: TelescopeControlManager

    @FXML private lateinit var serverTypeChoiceBox: ChoiceBox<TelescopeControlType>
    @FXML private lateinit var hostTextField: TextField
    @FXML private lateinit var portTextField: TextField
    @FXML private lateinit var connectButton: TwoStateButton

    init {
        title = "Telescope Control"
        resizable = false
    }

    override fun onCreate() {
        serverTypeChoiceBox.converter = TelescopeControlTypeStringConverter
        serverTypeChoiceBox.value = TelescopeControlType.STELLARIUM_JNOW
        serverTypeChoiceBox.valueProperty().on { telescopeControlManager.updateConnectionStatus() }

        telescopeControlManager.initialize()
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
        connectButton.state = connected

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

    private object TelescopeControlTypeStringConverter : StringConverter<TelescopeControlType>() {

        override fun toString(type: TelescopeControlType?) = type?.label ?: "No protocol selected"

        override fun fromString(string: String?) = null
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TelescopeControlWindow>()
    }
}
