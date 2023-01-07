package nebulosa.desktop.focusers

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.onOne
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialColor
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.focusers.Focuser
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject

class FocuserManagerScreen : Screen("FocuserManager", "nebulosa-focuser-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var focusers: ChoiceBox<Focuser>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var position: Label
    @FXML private lateinit var temperature: Label
    @FXML private lateinit var increment: Spinner<Double>
    @FXML private lateinit var absolute: Spinner<Double>
    @FXML private lateinit var moveIn: Button
    @FXML private lateinit var moveOut: Button
    @FXML private lateinit var moveTo: Button
    @FXML private lateinit var sync: Button
    @FXML private lateinit var abort: Button
    @FXML private lateinit var backlashCompensation: ToggleSwitch
    @FXML private lateinit var backlashCompensationSteps: Spinner<Double>
    @FXML private lateinit var autoFocus: Button

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Focuser"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedFocuser.isConnected.not()
        val isConnecting = equipmentManager.selectedFocuser.isConnecting
        val isMoving = equipmentManager.selectedFocuser.isMoving
        val isNotConnectedOrMoving = isNotConnected or isMoving

        focusers.converter = DeviceStringConverter()
        focusers.disableProperty().bind(isConnecting or isMoving)
        focusers.itemsProperty().bind(equipmentManager.attachedFocusers)
        equipmentManager.selectedFocuser.bind(focusers.selectionModel.selectedItemProperty())

        connect.disableProperty().bind(equipmentManager.selectedFocuser.isNull or isConnecting or isMoving)
        connect.textProperty().bind(equipmentManager.selectedFocuser.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        connect.textFillProperty().bind(equipmentManager.selectedFocuser.isConnected.between(MaterialColor.RED_700, MaterialColor.BLUE_GREY_700))

        position.textProperty().bind(equipmentManager.selectedFocuser.position.asString())

        // temperature.textProperty().bind(equipmentManager.selectedFocuser.temperature.asString())

        increment.disableProperty().bind(isNotConnectedOrMoving or !equipmentManager.selectedFocuser.canRelativeMove)

        moveIn.disableProperty().bind(increment.disableProperty())

        moveOut.disableProperty().bind(increment.disableProperty())

        absolute.disableProperty().bind(isNotConnectedOrMoving or !equipmentManager.selectedFocuser.canAbsoluteMove)

        equipmentManager.selectedFocuser.maxPosition.on {
            (increment.valueFactory as DoubleSpinnerValueFactory).max = it / 2.0
            (absolute.valueFactory as DoubleSpinnerValueFactory).max = it.toDouble()
        }

        moveTo.disableProperty().bind(absolute.disableProperty())

        sync.disableProperty().bind(isNotConnectedOrMoving or !equipmentManager.selectedFocuser.canSync)

        abort.disableProperty().bind(isNotConnectedOrMoving or !equipmentManager.selectedFocuser.canAbort)

        autoFocus.disableProperty().bind(isNotConnectedOrMoving)

        equipmentManager.selectedFocuser.onOne {
            title = "Focuser · ${it?.name}"
        }

        preferences.double("focuserManager.screen.x")?.let { x = it }
        preferences.double("focuserManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("focuserManager.screen.x", it) }
        yProperty().on { preferences.double("focuserManager.screen.y", it) }
    }

    override fun onStart() {}

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedFocuser.isConnected.get()) {
            equipmentManager.selectedFocuser.get().connect()
        } else {
            equipmentManager.selectedFocuser.get().disconnect()
        }
    }

    @FXML
    private fun moveIn() {
        val focuser = equipmentManager.selectedFocuser.get() ?: return
        focuser.moveFocusIn(increment.value.toInt())
    }

    @FXML
    private fun moveOut() {
        val focuser = equipmentManager.selectedFocuser.get() ?: return
        focuser.moveFocusOut(increment.value.toInt())
    }

    @FXML
    private fun moveTo() {
        val focuser = equipmentManager.selectedFocuser.get() ?: return
        focuser.moveFocusTo(absolute.value.toInt())
    }

    @FXML
    private fun sync() {
        val focuser = equipmentManager.selectedFocuser.get() ?: return
        focuser.syncFocusTo(absolute.value.toInt())
    }

    @FXML
    private fun abort() {
        val focuser = equipmentManager.selectedFocuser.get() ?: return
        focuser.abortFocus()
    }

    @FXML
    private fun openAutoFocus() {

    }
}
