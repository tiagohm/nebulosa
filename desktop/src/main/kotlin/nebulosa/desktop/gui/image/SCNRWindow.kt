package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import nebulosa.desktop.core.beans.isAnyOf
import nebulosa.desktop.core.beans.onZero
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.image.SCNRManager
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import org.controlsfx.control.ToggleSwitch

class SCNRWindow(private val window: ImageWindow) : AbstractWindow() {

    override val resourceName = "SCNR"

    override val icon = "nebulosa-scnr"

    @FXML private lateinit var channelChoiceBox: ChoiceBox<ImageChannel>
    @FXML private lateinit var protectionMethodChoiceBox: ChoiceBox<ProtectionMethod>
    @FXML private lateinit var enabledToggleSwitch: ToggleSwitch
    @FXML private lateinit var amountSpinner: Spinner<Double>

    private val scnrManager = SCNRManager(this)

    init {
        title = "SCNR"
        isResizable = false
    }

    override fun onCreate() {
        amountSpinner.disableProperty()
            .bind(protectionMethodChoiceBox.valueProperty().isAnyOf(ProtectionMethod.AVERAGE_NEUTRAL, ProtectionMethod.MAXIMUM_NEUTRAL))

        channelChoiceBox.valueProperty().onZero(scnrManager::apply)
        protectionMethodChoiceBox.valueProperty().onZero(scnrManager::apply)
        amountSpinner.valueProperty().onZero(scnrManager::apply)
        enabledToggleSwitch.selectedProperty().onZero(scnrManager::apply)
    }

    val amount
        get() = amountSpinner.value!!

    val protectionMethod
        get() = protectionMethodChoiceBox.value!!

    val channel
        get() = channelChoiceBox.value!!

    val enabled
        get() = enabledToggleSwitch.isSelected

    fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Double,
    ) {
        window.applySCNR(enabled, channel, protectionMethod, amount)
    }
}
