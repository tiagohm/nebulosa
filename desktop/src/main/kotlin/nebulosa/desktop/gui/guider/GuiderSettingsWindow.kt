package nebulosa.desktop.gui.guider

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.guider.GuiderSettingsManager
import nebulosa.desktop.view.guider.GuiderSettingsView
import org.controlsfx.control.ToggleSwitch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class GuiderSettingsWindow : AbstractWindow("GuiderSettings", "nebulosa-phd2"), GuiderSettingsView {

    @Lazy @Autowired private lateinit var guiderSettingsManager: GuiderSettingsManager

    @FXML private lateinit var hostTextField: TextField
    @FXML private lateinit var portTextField: TextField
    @FXML private lateinit var ditherSpinner: Spinner<Double>
    @FXML private lateinit var ditherInRAOnlyToggleSwitch: ToggleSwitch
    @FXML private lateinit var settlePixelToleranceSpinner: Spinner<Double>
    @FXML private lateinit var minimumSettleTimeSpinner: Spinner<Double>
    @FXML private lateinit var settleTimeoutSpinner: Spinner<Double>
    @FXML private lateinit var guidingStartRetryToggleSwitch: ToggleSwitch
    @FXML private lateinit var guidingStartTimeoutSpinner: Spinner<Double>
    @FXML private lateinit var roiPercentageToFindGuideStarSpinner: Spinner<Double>
    @FXML private lateinit var saveButton: Button

    init {
        title = "PHD2 Settings"
        resizable = false
    }

    override fun onStart() {
        guiderSettingsManager.loadPreferences()
    }

    override var host
        get() = hostTextField.text ?: ""
        set(value) {
            hostTextField.text = value
        }

    override var port
        get() = portTextField.text?.toIntOrNull() ?: 4400
        set(value) {
            portTextField.text = "$value"
        }

    override var dither
        get() = ditherSpinner.value!!
        set(value) {
            ditherSpinner.valueFactory.value = value
        }

    override var ditherInRAOnly
        get() = ditherInRAOnlyToggleSwitch.isSelected
        set(value) {
            ditherInRAOnlyToggleSwitch.isSelected = value
        }

    override var settlePixelTolerance
        get() = settlePixelToleranceSpinner.value!!
        set(value) {
            settlePixelToleranceSpinner.valueFactory.value = value
        }

    override var minimumSettleTime
        get() = minimumSettleTimeSpinner.value!!.toInt()
        set(value) {
            minimumSettleTimeSpinner.valueFactory.value = value.toDouble()
        }

    override var settleTimeout
        get() = settleTimeoutSpinner.value!!.toInt()
        set(value) {
            settleTimeoutSpinner.valueFactory.value = value.toDouble()
        }

    override var guidingStartRetry
        get() = guidingStartRetryToggleSwitch.isSelected
        set(value) {
            guidingStartRetryToggleSwitch.isSelected = value
        }

    override var guidingStartTimeout
        get() = guidingStartTimeoutSpinner.value!!.toInt()
        set(value) {
            guidingStartTimeoutSpinner.valueFactory.value = value.toDouble()
        }

    override var roiPercentageToFindGuideStar
        get() = roiPercentageToFindGuideStarSpinner.value!!
        set(value) {
            roiPercentageToFindGuideStarSpinner.valueFactory.value = value
        }

    @FXML
    private fun save() {
        guiderSettingsManager.save()
    }
}
