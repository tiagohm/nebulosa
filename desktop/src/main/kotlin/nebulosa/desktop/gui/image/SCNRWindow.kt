package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import nebulosa.desktop.core.beans.onZero
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.image.SCNRManager
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.image.SCNRView
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import org.controlsfx.control.ToggleSwitch

class SCNRWindow(private val view: ImageView) : AbstractWindow(), SCNRView {

    override val resourceName = "SCNR"

    override val icon = "nebulosa-scnr"

    @FXML private lateinit var channelChoiceBox: ChoiceBox<ImageChannel>
    @FXML private lateinit var protectionMethodChoiceBox: ChoiceBox<ProtectionMethod>
    @FXML private lateinit var enabledToggleSwitch: ToggleSwitch
    @FXML private lateinit var amountSpinner: Spinner<Double>

    private val scnrManager = SCNRManager(this)

    init {
        title = "SCNR"
        resizable = false
    }

    override fun onCreate() {
        channelChoiceBox.valueProperty().onZero(scnrManager::apply)
        protectionMethodChoiceBox.valueProperty().onZero(scnrManager::apply)
        amountSpinner.valueProperty().onZero(scnrManager::apply)
        enabledToggleSwitch.selectedProperty().onZero(scnrManager::apply)
    }

    override val amount
        get() = amountSpinner.value!!.toFloat()

    override val protectionMethod
        get() = protectionMethodChoiceBox.value!!

    override val channel
        get() = channelChoiceBox.value!!

    override val enabled
        get() = enabledToggleSwitch.isSelected

    override fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    ) {
        view.applySCNR(enabled, channel, protectionMethod, amount)
    }
}
