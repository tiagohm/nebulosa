package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.Mount
import org.koin.core.component.inject

class MountManagerScreen : Screen("MountManager", "nebulosa-mount-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var mounts: ChoiceBox<Mount>
    @FXML private lateinit var connect: Button

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Mount"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedMount.isConnected.not()

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
}
