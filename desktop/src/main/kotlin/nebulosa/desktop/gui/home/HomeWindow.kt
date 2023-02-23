package nebulosa.desktop.gui.home

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.CLOSE_CIRCLE_ICON
import nebulosa.desktop.gui.CONNECTION_ICON
import nebulosa.desktop.logic.between
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.home.HomeManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.logic.util.toggle
import nebulosa.desktop.view.home.HomeView
import org.springframework.beans.factory.annotation.Autowired

class HomeWindow(window: Stage) : AbstractWindow("Home", window = window), HomeView {

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

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var homeManager: HomeManager

    init {
        title = "Nebulosa"
        resizable = false
    }

    override fun onCreate() {
        hostTextField.disableProperty().bind(homeManager.connectedProperty)
        portTextField.disableProperty().bind(homeManager.connectedProperty)
        cameraButton.disableProperty().bind(!homeManager.connectedProperty or equipmentManager.attachedCameras.emptyProperty())
        mountButton.disableProperty().bind(!homeManager.connectedProperty or equipmentManager.attachedMounts.emptyProperty())
        guiderButton.disableProperty().bind(!homeManager.connectedProperty)
        filterWheelButton.disableProperty().bind(!homeManager.connectedProperty or equipmentManager.attachedFilterWheels.emptyProperty())
        focuserButton.disableProperty().bind(!homeManager.connectedProperty or equipmentManager.attachedFocusers.emptyProperty())
        domeButton.disableProperty().bind(!homeManager.connectedProperty)
        rotatorButton.disableProperty().bind(!homeManager.connectedProperty)
        switchButton.disableProperty().bind(!homeManager.connectedProperty)
        alignmentButton.disableProperty().bind(!homeManager.connectedProperty)
        sequencerButton.disableProperty().bind(!homeManager.connectedProperty)
        indiButton.disableProperty().bind(!homeManager.connectedProperty)

        connectButton.textProperty().bind(homeManager.connectedProperty.between(CLOSE_CIRCLE_ICON, CONNECTION_ICON))
        homeManager.connectedProperty.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700", it) }
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
