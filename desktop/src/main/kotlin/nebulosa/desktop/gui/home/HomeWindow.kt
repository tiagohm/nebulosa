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
import nebulosa.desktop.view.home.HomeView

class HomeWindow : AbstractWindow(), HomeView {

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
        resizable = false
    }

    override fun onCreate() {
        hostTextField.disableProperty().bind(homeManager.connected)
        portTextField.disableProperty().bind(homeManager.connected)
        cameraButton.disableProperty().bind(!homeManager.connected)
        mountButton.disableProperty().bind(!homeManager.connected)
        guiderButton.disableProperty().bind(!homeManager.connected)
        filterWheelButton.disableProperty().bind(!homeManager.connected)
        focuserButton.disableProperty().bind(!homeManager.connected)
        domeButton.disableProperty().bind(!homeManager.connected)
        rotatorButton.disableProperty().bind(!homeManager.connected)
        switchButton.disableProperty().bind(!homeManager.connected)
        alignmentButton.disableProperty().bind(!homeManager.connected)
        sequencerButton.disableProperty().bind(!homeManager.connected)
        indiButton.disableProperty().bind(!homeManager.connected)

        connectButton.textProperty().bind(homeManager.connected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        homeManager.connected.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700", it) }
    }

    override fun onStart() {
        homeManager.loadPreferences()
    }

    override fun onStop() {
        homeManager.close()
    }

    override var host
        get() = hostTextField.text.trim().ifBlank { "localhost" }
        set(value) {
            hostTextField.text = value.trim()
        }

    override var port
        get() = portTextField.text?.toIntOrNull() ?: 7624
        set(value) {
            portTextField.text = "$value"
        }

    @FXML
    @Synchronized
    private fun connect() {
        homeManager.connect()
    }

    @FXML
    @Synchronized
    private fun open(event: ActionEvent) {
        homeManager.open((event.source as Node).userData as String)
    }
}
