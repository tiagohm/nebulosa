package nebulosa.desktop.gui.home

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.CLOSE_CIRCLE_ICON
import nebulosa.desktop.gui.CONNECTION_ICON
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.between
import nebulosa.desktop.logic.home.HomeManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.logic.util.toggle
import nebulosa.desktop.view.home.HomeView
import org.koin.core.component.inject

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

    private val equipmentManager by inject<EquipmentManager>()
    private val homeManager = HomeManager(this)

    init {
        title = "Nebulosa"
        resizable = false
    }

    override fun onCreate() {
        hostTextField.disableProperty().bind(homeManager.connected)
        portTextField.disableProperty().bind(homeManager.connected)
        cameraButton.disableProperty().bind(!homeManager.connected or equipmentManager.attachedCameras.emptyProperty())
        mountButton.disableProperty().bind(!homeManager.connected or equipmentManager.attachedMounts.emptyProperty())
        guiderButton.disableProperty().bind(!homeManager.connected)
        filterWheelButton.disableProperty().bind(!homeManager.connected or equipmentManager.attachedFilterWheels.emptyProperty())
        focuserButton.disableProperty().bind(!homeManager.connected or equipmentManager.attachedFocusers.emptyProperty())
        domeButton.disableProperty().bind(!homeManager.connected)
        rotatorButton.disableProperty().bind(!homeManager.connected)
        switchButton.disableProperty().bind(!homeManager.connected)
        alignmentButton.disableProperty().bind(!homeManager.connected)
        sequencerButton.disableProperty().bind(!homeManager.connected)
        indiButton.disableProperty().bind(!homeManager.connected)

        connectButton.textProperty().bind(homeManager.connected.between(CLOSE_CIRCLE_ICON, CONNECTION_ICON))
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
