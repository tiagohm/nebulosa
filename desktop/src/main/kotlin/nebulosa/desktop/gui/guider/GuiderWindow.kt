package nebulosa.desktop.gui.guider

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.guider.GuiderManager
import nebulosa.desktop.logic.isNull
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.indi.device.camera.Camera
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class GuiderWindow : AbstractWindow("Guider", "target"), GuiderView {

    @Lazy @Autowired private lateinit var guiderManager: GuiderManager

    @FXML private lateinit var guiderChoiceBox: ChoiceBox<Camera>
    @FXML private lateinit var connectButton: TwoStateButton
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var startButton: Button
    @FXML private lateinit var stopButton: Button

    init {
        title = "Guider"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !guiderManager.connectedProperty
        val isConnecting = guiderManager.connectingProperty
        val isGuiding = guiderManager.guidingProperty
        val isNotConnectedOrGuiding = isNotConnected or isGuiding

        guiderManager.initialize()

        guiderChoiceBox.converter = GuiderStringConverter
        guiderChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guiderChoiceBox.itemsProperty().bind(guiderManager.cameras)
        guiderManager.bind(guiderChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(guiderManager.isNull() or isConnecting or isGuiding)
        guiderManager.connectedProperty.on { connectButton.state = it }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        startButton.disableProperty().bind(isNotConnectedOrGuiding)
        stopButton.disableProperty().bind(isNotConnected or !isGuiding)
    }

    @FXML
    private fun connect() {
        guiderManager.connect()
    }

    @FXML
    private fun openINDIPanelControl() {
        guiderManager.openINDIPanelControl()
    }

    @FXML
    private fun start() {
        guiderManager.start()
    }

    @FXML
    private fun stop() {
        guiderManager.stop()
    }

    private object GuiderStringConverter : StringConverter<Camera>() {

        override fun toString(device: Camera?) = device?.name ?: "No guiding camera selected"

        override fun fromString(text: String?) = null
    }
}
