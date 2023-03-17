package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.SwitchSegmentedButton
import nebulosa.desktop.logic.image.SCNRManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.image.SCNRView
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod

class SCNRWindow(private val view: ImageView) : AbstractWindow("SCNR", "rgb"), SCNRView {

    @FXML private lateinit var channelChoiceBox: ChoiceBox<ImageChannel>
    @FXML private lateinit var protectionMethodChoiceBox: ChoiceBox<ProtectionMethod>
    @FXML private lateinit var enabledSwitch: SwitchSegmentedButton
    @FXML private lateinit var amountSpinner: Spinner<Double>

    private val scnrManager = SCNRManager(this)

    init {
        title = "SCNR"
        resizable = false
    }

    override fun onCreate() {
        channelChoiceBox.valueProperty().on { scnrManager.apply() }
        protectionMethodChoiceBox.valueProperty().on { scnrManager.apply() }
        amountSpinner.valueProperty().on { scnrManager.apply() }
        enabledSwitch.stateProperty.on { scnrManager.apply() }
    }

    override val amount
        get() = amountSpinner.value!!.toFloat()

    override val protectionMethod
        get() = protectionMethodChoiceBox.value!!

    override val channel
        get() = channelChoiceBox.value!!

    override val enabled
        get() = enabledSwitch.state

    override fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    ) {
        view.scnr(enabled, channel, protectionMethod, amount)
    }
}
