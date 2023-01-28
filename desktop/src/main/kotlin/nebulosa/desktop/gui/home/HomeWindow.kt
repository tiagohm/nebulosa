package nebulosa.desktop.gui.home

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.home.HomeManager

class HomeWindow : AbstractWindow() {

    override val resourceName = "Home"

    override val icon = "nebulosa"

    @FXML private lateinit var hostTextField: TextField
    @FXML private lateinit var portTextField: TextField
    @FXML private lateinit var connectButton: Button
    @FXML private lateinit var cameraButton: Button
    @FXML private lateinit var mountButton: Button
    @FXML private lateinit var guiderButton: Button
    @FXML private lateinit var filterWheelButton: Button
    @FXML private lateinit var focuserButton: Button
    @FXML private lateinit var domeButton: Button
    @FXML private lateinit var rotatorButton: Button
    @FXML private lateinit var switchButton: Button
    @FXML private lateinit var atlasButton: Button
    @FXML private lateinit var plateSolvingButton: Button
    @FXML private lateinit var alignmentButton: Button
    @FXML private lateinit var sequencerButton: Button
    @FXML private lateinit var imageViewerButton: Button
    @FXML private lateinit var indiButton: Button

    private val homeManager = HomeManager(this)

    init {
        title = "Nebulosa"
        isResizable = false
    }

    override fun onCreate() {
        hostTextField.disableProperty().bind(homeManager.isConnected)
        portTextField.disableProperty().bind(homeManager.isConnected)
        cameraButton.disableProperty().bind(!homeManager.isConnected)
        mountButton.disableProperty().bind(!homeManager.isConnected)
        guiderButton.disableProperty().bind(!homeManager.isConnected)
        filterWheelButton.disableProperty().bind(!homeManager.isConnected)
        focuserButton.disableProperty().bind(!homeManager.isConnected)
        domeButton.disableProperty().bind(!homeManager.isConnected)
        rotatorButton.disableProperty().bind(!homeManager.isConnected)
        switchButton.disableProperty().bind(!homeManager.isConnected)
        alignmentButton.disableProperty().bind(!homeManager.isConnected)
        sequencerButton.disableProperty().bind(!homeManager.isConnected)
        indiButton.disableProperty().bind(!homeManager.isConnected)

        connectButton.textProperty().bind(homeManager.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        homeManager.isConnected.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700") }
    }

    override fun onStart() {
        homeManager.loadPreferences()
    }

    override fun onStop() {
        homeManager.close()
    }

    var host
        get() = hostTextField.text.trim().ifBlank { "localhost" }
        set(value) {
            hostTextField.text = value.trim()
        }

    var port
        get() = portTextField.text?.toIntOrNull() ?: 7624
        set(value) {
            portTextField.text = "$value"
        }

    @FXML
    @Synchronized
    private fun connect() {
        try {
            homeManager.connect()
        } catch (e: Throwable) {
            showAlert(
                "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                "Connection failed"
            )
        }
    }

    @FXML
    @Synchronized
    private fun open(event: ActionEvent) {
        homeManager.open((event.source as Node).userData as String)
    }
}
