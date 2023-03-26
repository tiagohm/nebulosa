package nebulosa.desktop.gui.guider

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.input.MouseButton
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.guider.GuiderManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class GuiderWindow : AbstractWindow("Guider", "target"), GuiderView {

    @Lazy @Autowired private lateinit var guiderManager: GuiderManager

    @FXML private lateinit var guideCameraChoiceBox: ChoiceBox<Camera>
    @FXML private lateinit var connectGuideCameraButton: TwoStateButton
    @FXML private lateinit var openINDIForGuideCameraButton: Button
    @FXML private lateinit var guideMountChoiceBox: ChoiceBox<Mount>
    @FXML private lateinit var connectGuideMountButton: TwoStateButton
    @FXML private lateinit var openINDIForGuideMountButton: Button
    @FXML private lateinit var startLoopingButton: Button
    @FXML private lateinit var stopLoopingButton: Button

    init {
        title = "Guider"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !guiderManager.selectedGuideCamera.connectedProperty or !guiderManager.selectedGuideMount.connectedProperty
        val isConnecting = guiderManager.selectedGuideCamera.connectingProperty or guiderManager.selectedGuideMount.connectingProperty
        val isGuiding = guiderManager.guidingProperty
        val isNotConnectedOrGuiding = isNotConnected or isGuiding

        guiderManager.initialize()

        guideCameraChoiceBox.converter = GuideCameraStringConverter
        guideCameraChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guideCameraChoiceBox.itemsProperty().bind(guiderManager.cameras)
        guideCameraChoiceBox.selectionModel.selectedItemProperty().on { guiderManager.selectedGuideCamera.set(it) }

        connectGuideCameraButton.disableProperty()
            .bind(guiderManager.selectedGuideCamera.isNull or guiderManager.selectedGuideMount.isNull or isConnecting or isGuiding)
        guiderManager.selectedGuideCamera.connectedProperty.on { connectGuideCameraButton.state = it }

        openINDIForGuideCameraButton.disableProperty().bind(connectGuideCameraButton.disableProperty())

        guideMountChoiceBox.converter = GuideMountStringConverter
        guideMountChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guideMountChoiceBox.itemsProperty().bind(guiderManager.mounts)
        guideMountChoiceBox.selectionModel.selectedItemProperty().on { guiderManager.selectedGuideMount.set(it) }

        connectGuideMountButton.disableProperty()
            .bind(guiderManager.selectedGuideMount.isNull or guiderManager.selectedGuideMount.isNull or isConnecting or isGuiding)
        guiderManager.selectedGuideMount.connectedProperty.on { connectGuideMountButton.state = it }

        openINDIForGuideMountButton.disableProperty().bind(connectGuideMountButton.disableProperty())

        startLoopingButton.disableProperty().bind(isNotConnectedOrGuiding)
        stopLoopingButton.disableProperty().bind(isNotConnected or !isGuiding)
    }

    @FXML
    private fun connectGuideCamera() {
        guiderManager.connectGuideCamera()
    }

    @FXML
    private fun connectGuideMount() {
        guiderManager.connectGuideMount()
    }

    @FXML
    private fun openINDIForGuideCamera() {
        guiderManager.openINDIPanelControlForGuideCamera()
    }

    @FXML
    private fun openINDIForGuideMount() {
        guiderManager.openINDIPanelControlForGuideMount()
    }

    @FXML
    private fun startLooping() {
        guiderManager.startLooping()
    }

    @FXML
    private fun stopLooping() {
        guiderManager.stopLooping()
    }

    override fun onMouseClicked(
        button: MouseButton,
        clickCount: Int,
        isControlDown: Boolean, isShiftDown: Boolean, isAltDown: Boolean,
        mouseX: Double, mouseY: Double,
        imageX: Double, imageY: Double,
    ) {
        if (button == MouseButton.PRIMARY && clickCount == 1) {
            if (!isControlDown && !isShiftDown && !isAltDown) {
                guiderManager.selectGuideStar(imageX, imageY)
            } else if (!isControlDown && isShiftDown && !isAltDown) {
                guiderManager.deselectGuideStar()
            }
        }
    }

    private object GuideCameraStringConverter : StringConverter<Camera>() {

        override fun toString(device: Camera?) = device?.name ?: "No guiding camera selected"

        override fun fromString(text: String?) = null
    }

    private object GuideMountStringConverter : StringConverter<Mount>() {

        override fun toString(device: Mount?) = device?.name ?: "No guiding mount selected"

        override fun fromString(text: String?) = null
    }
}
