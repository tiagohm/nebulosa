package nebulosa.desktop.gui.focuser

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.focuser.FocuserManager
import nebulosa.indi.device.focusers.Focuser
import org.controlsfx.control.ToggleSwitch

class FocuserWindow : AbstractWindow() {

    override val resourceName = "Focuser"

    override val icon = "nebulosa-focuser"

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

    private val focuserManager = FocuserManager(this)

    init {
        title = "Focuser"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = focuserManager.isConnected.not()
        val isConnecting = focuserManager.isConnecting
        val isMoving = focuserManager.isMoving
        val isNotConnectedOrMoving = isNotConnected or isMoving

        focuserChoiceBox.converter = DeviceStringConverter()
        focuserChoiceBox.disableProperty().bind(isConnecting or isMoving)
        focuserChoiceBox.itemsProperty().bind(focuserManager.focusers)
        focuserManager.bind(focuserChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(focuserManager.isNull or isConnecting or isMoving)
        connectButton.textProperty().bind(focuserManager.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        focuserManager.isConnected.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700") }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        positionLabel.textProperty().bind(focuserManager.position.asString())

        temperatureLabel.textProperty().bind(focuserManager.temperature.asString("%.01f °C"))

        incrementSpinner.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canRelativeMove)

        moveInButton.disableProperty().bind(incrementSpinner.disableProperty())

        moveOutButton.disableProperty().bind(incrementSpinner.disableProperty())

        absoluteSpinner.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canAbsoluteMove)

        moveToButton.disableProperty().bind(absoluteSpinner.disableProperty())

        syncButton.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canSync)

        abortButton.disableProperty().bind(isNotConnectedOrMoving or !focuserManager.canAbort)

        autoFocusButton.disableProperty().bind(isNotConnectedOrMoving)

        focuserManager.loadPreferences(null)
    }

    override fun onStart() {
        focuserManager.loadPreferences()
    }

    override fun onStop() {
        focuserManager.savePreferences(null)
        focuserManager.close()
    }

    var status
        get() = statusLabel.text!!
        set(value) {
            statusLabel.text = value
        }

    var position
        get() = positionLabel.text!!
        set(value) {
            positionLabel.text = value
        }

    var temperature
        get() = temperatureLabel.text!!
        set(value) {
            temperatureLabel.text = value
        }

    var increment
        get() = incrementSpinner.value.toInt()
        set(value) {
            incrementSpinner.valueFactory.value = value.toDouble()
        }

    var maxIncrement
        get() = (incrementSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (incrementSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var absolute
        get() = absoluteSpinner.value.toInt()
        set(value) {
            absoluteSpinner.valueFactory.value = value.toDouble()
        }

    var absoluteMax
        get() = (absoluteSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (absoluteSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    @FXML
    private fun connect() {
        focuserManager.connect()
    }

    @FXML
    private fun openINDI() {
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
    private fun openAutoFocus() {
    }

    companion object {

        @Volatile private var window: FocuserWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = FocuserWindow()
            window!!.open(bringToFront = true)
        }
    }
}
