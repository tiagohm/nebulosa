package nebulosa.desktop.cameras

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import nebulosa.desktop.core.controls.Screen
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import org.controlsfx.control.ToggleSwitch

class SCNRScreen(private val imageViewer: ImageViewerScreen) : Screen("SCNR", "nebulosa-scnr") {

    @FXML private lateinit var channel: ChoiceBox<ImageChannel>
    @FXML private lateinit var protectionMethod: ChoiceBox<ProtectionMethod>
    @FXML private lateinit var autoApply: ToggleSwitch
    @FXML private lateinit var amount: Spinner<Double>

    init {
        title = "SCNR"
        isResizable = false
    }

    override fun onCreate() {
        amount.disableProperty().bind(
            protectionMethod.valueProperty().isEqualTo(ProtectionMethod.AVERAGE_NEUTRAL).or(
                protectionMethod.valueProperty().isEqualTo(ProtectionMethod.MAXIMUM_NEUTRAL)
            )
        )

        autoApply.selectedProperty().addListener { _, _, value -> imageViewer.scnrAutoApply = value }
    }

    override fun onStart() {
        channel.value = imageViewer.scnrChannel
        protectionMethod.value = imageViewer.scnrProtectionMode
        amount.valueFactory.value = imageViewer.scnrAmount.toDouble()
        autoApply.isSelected = imageViewer.scnrAutoApply
    }

    @FXML
    private fun apply() {
        imageViewer.transformImage(
            scnr = true, scnrChannel = channel.value,
            scnrProtectionMode = protectionMethod.value,
            scnrAmount = amount.value.toFloat(),
        )
    }
}
