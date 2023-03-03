package nebulosa.desktop.gui.focuser

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.between
import nebulosa.desktop.logic.focuser.FocuserManager
import nebulosa.desktop.logic.isNull
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.focuser.FocuserView
import nebulosa.indi.device.focuser.Focuser
import org.controlsfx.control.ToggleSwitch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class FocuserWindow : AbstractWindow("Focuser", "nebulosa-focuser"), FocuserView {

    @Lazy @Autowired private lateinit var focuserManager: FocuserManager

    @FXML private lateinit var focuserChoiceBox: ChoiceBox<Focuser>
    @FXML private lateinit var connectButton: Button
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var positionLabel: Label
    @FXML private lateinit var temperatureLabel: Label
    @FXML private lateinit var statusLabel: Label
    @FXML private lateinit var incrementSpinner: Spinner<Double>
    @FXML private lateinit var absoluteSpinner: Spinner<Double>
    @FXML private lateinit var moveInButton: Button
    @FXML private lateinit var moveOutButton: Button
    @FXML private lateinit var moveToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var abortButton: Button
    @FXML private lateinit var backlashCompensationToggleSwitch: ToggleSwitch
    @FXML private lateinit var backlashCompensationStepsSpinner: Spinner<Double>
    @FXML private lateinit var autoFocusButton: Button

    init {
        title = "Focuser"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = focuserManager.connectedProperty.not()
        val isConnecting = focuserManager.connectingProperty
        val isMoving = focuserManager.movingProperty
        val isNotConnectedOrMoving = isNotConnected or isMoving

        focuserManager.initialize()

        focuserChoiceBox.converter = FocuserStringConverter
        focuserChoiceBox.disableProperty().bind(isConnecting or isMoving)
        focuserChoiceBox.itemsProperty().bind(focuserManager.focusers)
        focuserManager.bind(focuserChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(focuserManager.isNull() or isConnecting or isMoving)
        connectButton.textProperty().bind(focuserManager.connectedProperty.between("󰅙", "󱘖"))
        focuserManager.connectedProperty.between(connectButton.styleClass, "text-red-700", "text-blue-grey-700")

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        positionLabel.textProperty().bind(focuserManager.positionProperty.asString())

        temperatureLabel.textProperty().bind(focuserManager.temperatureProperty.asString("%.01f °C"))

        incrementSpinner.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canRelativeMoveProperty)

        moveInButton.disableProperty().bind(incrementSpinner.disableProperty())

        moveOutButton.disableProperty().bind(incrementSpinner.disableProperty())

        absoluteSpinner.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canAbsoluteMoveProperty)

        moveToButton.disableProperty().bind(absoluteSpinner.disableProperty())

        syncButton.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canSyncProperty)

        abortButton.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canAbortProperty)

        autoFocusButton.disableProperty().bind(isNotConnectedOrMoving)
    }

    override fun onStart() {
        focuserManager.loadPreferences()
    }

    override fun onStop() {
        focuserManager.savePreferences()
    }

    override val status
        get() = statusLabel.text!!

    override val increment
        get() = incrementSpinner.value.toInt()

    override val maxIncrement
        get() = (incrementSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override val absolute
        get() = absoluteSpinner.value.toInt()

    override val absoluteMax
        get() = (absoluteSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override fun updateStatus(status: String) {
        statusLabel.text = status
    }

    override fun updateMaxIncrement(value: Int) {
        (incrementSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
    }

    override fun updateAbsoluteMax(value: Int) {
        (absoluteSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
    }

    @FXML
    private fun connect() {
        focuserManager.connect()
    }

    @FXML
    private fun openINDIPanelControl() {
        focuserManager.openINDIPanelControl()
    }

    @FXML
    private fun moveIn() {
        focuserManager.moveIn()
    }

    @FXML
    private fun moveOut() {
        focuserManager.moveOut()
    }

    @FXML
    private fun moveTo() {
        focuserManager.moveTo()
    }

    @FXML
    private fun sync() {
        focuserManager.sync()
    }

    @FXML
    private fun abort() {
        focuserManager.abort()
    }

    @FXML
    private fun openAutoFocus() = Unit

    private object FocuserStringConverter : StringConverter<Focuser>() {

        override fun toString(device: Focuser?) = device?.name ?: "No focuser selected"

        override fun fromString(text: String?) = null
    }
}
