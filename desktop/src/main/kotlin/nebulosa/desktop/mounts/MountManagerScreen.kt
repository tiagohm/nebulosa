package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.Mount
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject

class MountManagerScreen : Screen("MountManager", "nebulosa-mount-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var mounts: ChoiceBox<Mount>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var pierSide: Label
    @FXML private lateinit var tracking: ToggleSwitch

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Mount"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedMount.isConnected.not()
        val isConnecting = equipmentManager.selectedMount.isConnecting
        val isSlewing = SimpleBooleanProperty(false)
        val isNotConnectedOrSlewing = isNotConnected.or(isSlewing)

        mounts.disableProperty().bind(isConnecting.or(isSlewing))
        mounts.itemsProperty().bind(equipmentManager.attachedMounts)
        equipmentManager.selectedMount.bind(mounts.selectionModel.selectedItemProperty())

        connect.disableProperty().bind(equipmentManager.selectedMount.isNull.or(isConnecting).or(isSlewing))

        pierSide.textProperty().bind(equipmentManager.selectedMount.pierSide.asString())

        tracking.disableProperty().bind(isNotConnectedOrSlewing)
        equipmentManager.selectedMount.isTracking.addListener { _, _, value -> tracking.isSelected = value }
        tracking.selectedProperty().addListener { _, _, value -> equipmentManager.selectedMount.value.tracking(value) }

        equipmentManager.selectedMount.addListener { _, _, value ->
            title = "Mount · ${value.name}"
        }

        equipmentManager.selectedMount.isConnected.addListener { _, _, value ->
            connect.graphic = if (value) Icon.closeCircle() else Icon.connection()
        }

        preferences.double("mountManager.screen.x")?.let { x = it }
        preferences.double("mountManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("mountManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("mountManager.screen.y", value.toDouble()) }
    }

    override fun onStart() {
        subscriber = eventBus
            .filter { it is DeviceEvent<*> }
            .subscribe(this)

        val mount = equipmentManager.selectedMount.value

        if (mount !in equipmentManager.attachedMounts) {
            mounts.selectionModel.select(null)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedMount.isConnected.value) {
            equipmentManager.selectedMount.value!!.connect()
        } else {
            equipmentManager.selectedMount.value!!.disconnect()
        }
    }

    private fun updateStatus() {
        val mount = equipmentManager.selectedMount.value ?: return

        val text = if (mount.isParking) "parking"
        else if (mount.isParked) "parked"
        else ""
    }
}
