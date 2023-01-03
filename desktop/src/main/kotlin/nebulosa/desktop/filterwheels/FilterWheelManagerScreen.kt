package nebulosa.desktop.filterwheels

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import nebulosa.desktop.core.controls.ButtonValueFactory
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.filterwheels.FilterWheel
import org.koin.core.component.inject
import java.util.concurrent.Executors

class FilterWheelManagerScreen : Screen("FilterWheelManager", "nebulosa-fw-manager") {

    private val equipmentManager by inject<EquipmentManager>()
    private val executor = Executors.newSingleThreadExecutor()

    @FXML private lateinit var filterWheels: ChoiceBox<FilterWheel>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var filterSlots: TableView<Int>

    private val connecting = SimpleBooleanProperty(false)

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Filter Wheel"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedFilterWheel.isConnected.not()
        val isMoving = equipmentManager.selectedFilterWheel.isMoving
        val isNotConnectedOrMoving = isNotConnected.or(isMoving)
        filterWheels.disableProperty().bind(connecting.or(isMoving))
        equipmentManager.selectedFilterWheel.bind(filterWheels.selectionModel.selectedItemProperty())
        connect.disableProperty().bind(equipmentManager.selectedFilterWheel.isNull.or(connecting).or(isMoving))
        filterSlots.disableProperty().bind(isNotConnectedOrMoving)

        filterSlots.columns[0].cellValueFactory = FilterSlotValueFactory(0)
        filterSlots.columns[1].cellFactory = TextFieldTableCell.forTableColumn()
        filterSlots.columns[1].cellValueFactory = FilterSlotValueFactory(1)
        filterSlots.columns[1].setOnEditCommit {
            val label = it.newValue as? String
            if (label.isNullOrBlank()) return@setOnEditCommit
            val filterWheel = equipmentManager.selectedFilterWheel.value ?: return@setOnEditCommit
            val position = it.tableView.items[it.tablePosition.row]
            preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label", label)
        }
        filterSlots.columns[2].cellFactory = ButtonValueFactory<Int, String> { position, node ->
            val button = node as? Button

            button?.apply {
                disableProperty().unbind()
                onAction = null
            }

            (button ?: Button("Move")).apply {
                cursor = Cursor.HAND
                disableProperty().bind(equipmentManager.selectedFilterWheel.position.isEqualTo(position))
                setOnAction { equipmentManager.selectedFilterWheel.value.moveTo(position) }
            }
        }

        equipmentManager.selectedFilterWheel.addListener { _, _, value ->
            title = "Filter Wheel · ${value.name}"
        }

        equipmentManager.selectedFilterWheel.slotCount.addListener { _, _, count ->
            filterSlots.items.setAll((1..count.toInt()).toList())
            height = 64.0 + (count.toInt() + 1) * 40.0
        }

        equipmentManager.selectedFilterWheel.isConnected.addListener { _, _, value ->
            connecting.set(false)

            connect.graphic = if (value) Icon.closeCircle() else Icon.connection()
        }

        preferences.double("filterWheelManager.screen.x")?.let { x = it }
        preferences.double("filterWheelManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("filterWheelManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("filterWheelManager.screen.y", value.toDouble()) }
    }

    override fun onStart() {
        subscriber = eventBus
            .filter { it is DeviceEvent<*> }
            .subscribe(this)

        val filterWheel = equipmentManager.selectedFilterWheel.value

        filterWheels.items.addAll(equipmentManager.attachedFilterWheels.filter { it !in filterWheels.items })
        filterWheels.items.removeAll(filterWheels.items.filter { it !in equipmentManager.attachedFilterWheels })

        if (filterWheel !in equipmentManager.attachedFilterWheels) {
            filterWheels.selectionModel.select(null)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null

        executor.shutdownNow()
    }

    override fun onEvent(event: Any) {
        if (event is DeviceEvent<*>
            && event.device === equipmentManager.selectedFilterWheel.value
        ) {
            when (event) {

            }
        }
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedFilterWheel.isConnected.value) {
            connecting.set(true)
            equipmentManager.selectedFilterWheel.value!!.connect()
        } else {
            equipmentManager.selectedFilterWheel.value!!.disconnect()
        }
    }

    private inner class FilterSlotValueFactory(val index: Int) : Callback<TableColumn.CellDataFeatures<Int, Any>, ObservableValue<out Any>> {

        override fun call(param: TableColumn.CellDataFeatures<Int, Any>): ObservableValue<out Any>? {
            val filterWheel = equipmentManager.selectedFilterWheel.value ?: return null

            return when (index) {
                0 -> ReadOnlyIntegerWrapper(param.value)
                1 -> {
                    val label = preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.${param.value}.label") ?: ""
                    ReadOnlyStringWrapper(label.ifBlank { "Filter #${param.value}" })
                }
                else -> null
            }
        }
    }
}
