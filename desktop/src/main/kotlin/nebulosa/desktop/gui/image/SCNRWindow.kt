package nebulosa.desktop.gui.image

import javafx.animation.PauseTransition
import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import javafx.util.Duration
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
    private val transformer = PauseTransition(Duration.seconds(0.5))

    init {
        title = "SCNR"
        resizable = false

        transformer.setOnFinished { launch { scnrManager.apply() } }
    }

    override suspend fun onCreate() {
        channelChoiceBox.valueProperty().on { transformer.playFromStart() }
        protectionMethodChoiceBox.valueProperty().on { transformer.playFromStart() }
        amountSpinner.valueProperty().on { transformer.playFromStart() }
        enabledSwitch.stateProperty.on { transformer.playFromStart() }
    }

    override val amount
        get() = amountSpinner.value!!.toFloat()

    override val protectionMethod
        get() = protectionMethodChoiceBox.value!!

    override val channel
        get() = channelChoiceBox.value!!

    override val enabled
        get() = enabledSwitch.state

    override suspend fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    ) {
        view.scnr(enabled, channel, protectionMethod, amount)
    }
}
