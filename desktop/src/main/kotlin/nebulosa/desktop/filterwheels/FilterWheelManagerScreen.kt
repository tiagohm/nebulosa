package nebulosa.desktop.filterwheels

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import nebulosa.desktop.core.controls.ButtonValueFactory
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.filterwheels.FilterWheel
import org.koin.core.component.inject

class FilterWheelManagerScreen : Screen("FilterWheelManager", "nebulosa-fw-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var filterWheels: ChoiceBox<FilterWheel>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var useFilterWheelAsShutter: CheckBox
    @FXML private lateinit var filterAsShutter: ChoiceBox<String>
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
        filterWheels.itemsProperty().bind(equipmentManager.attachedFilterWheels)
        equipmentManager.selectedFilterWheel.bind(filterWheels.selectionModel.selectedItemProperty())
        connect.disableProperty().bind(equipmentManager.selectedFilterWheel.isNull.or(connecting).or(isMoving))
        filterSlots.disableProperty().bind(isNotConnectedOrMoving)
        useFilterWheelAsShutter.disableProperty().bind(isNotConnectedOrMoving)
        filterAsShutter.disableProperty().bind(useFilterWheelAsShutter.disableProperty().or(useFilterWheelAsShutter.selectedProperty().not()))

        filterSlots.columns[0].cellValueFactory = FilterSlotValueFactory(0)
        filterSlots.columns[1].cellFactory = TextFieldTableCell.forTableColumn()
        filterSlots.columns[1].cellValueFactory = FilterSlotValueFactory(1)

        filterSlots.columns[1].setOnEditCommit {
            val label = it.newValue as? String
            if (label.isNullOrBlank()) return@setOnEditCommit
            val filterWheel = equipmentManager.selectedFilterWheel.value ?: return@setOnEditCommit
            val position = it.tableView.items[it.tablePosition.row]
            preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label", label)
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        filterSlots.columns[2].cellFactory = object : ButtonValueFactory<Int, String> {

            override fun cell(item: Int, node: Node?): Node {
                val button = node as? Button

                node?.also(::dispose)

                return (button ?: Button("Move")).apply {
                    cursor = Cursor.HAND
                    disableProperty().bind(equipmentManager.selectedFilterWheel.position.isEqualTo(item))
                    setOnAction { equipmentManager.selectedFilterWheel.value.moveTo(item) }
                }
            }

            override fun dispose(node: Node) {
                node as Button
                node.disableProperty().unbind()
                node.onAction = null
            }
        }

        filterAsShutter.selectionModel.selectedIndexProperty().addListener { _, _, index ->
            val filterWheel = equipmentManager.selectedFilterWheel.value ?: return@addListener
            preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter", index.toInt())
        }

        equipmentManager.selectedFilterWheel.addListener { _, _, _ ->
            updateTitle()
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        equipmentManager.selectedFilterWheel.position.addListener { _, _, _ -> updateTitle() }

        equipmentManager.selectedFilterWheel.slotCount.addListener { _, _, count ->
            filterSlots.items.setAll((1..count.toInt()).toList())
            height = 180.0 + count.toInt() * 29.9
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

        if (filterWheel !in equipmentManager.attachedFilterWheels) {
            filterWheels.selectionModel.select(null)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
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

    @FXML
    private fun toggleUseFilterWheelAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.value ?: return
        preferences.bool("filterWheelManager.equipment.${filterWheel.name}.useFilterWheelAsShutter", useFilterWheelAsShutter.isSelected)
    }

    private fun computeFilterName(position: Int): String {
        val filterWheel = equipmentManager.selectedFilterWheel.value ?: return "Filter #$position"
        val label = preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    private fun updateTitle() {
        val filterWheel = equipmentManager.selectedFilterWheel.value ?: return
        val position = equipmentManager.selectedFilterWheel.position.value
        if (position < 0) return
        title = "Filter Wheel · ${filterWheel.name} · ${computeFilterName(position)}"
    }

    private fun updateUseFilterWheelAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.value ?: return
        useFilterWheelAsShutter.isSelected = preferences.bool("filterWheelManager.equipment.${filterWheel.name}.useFilterWheelAsShutter")
        val selectedFilterAsShutter = preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter") ?: 0
        filterAsShutter.items.setAll((1..filterWheel.slotCount).map(::computeFilterName))
        filterAsShutter.selectionModel.select(selectedFilterAsShutter)
    }

    private fun updateFilterAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.value ?: return
        val selectedFilterAsShutter = preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter") ?: -1
        if (selectedFilterAsShutter in filterAsShutter.items.indices) filterAsShutter.selectionModel.select(selectedFilterAsShutter)
        else filterAsShutter.selectionModel.selectFirst()
    }

    private inner class FilterSlotValueFactory(val index: Int) : Callback<TableColumn.CellDataFeatures<Int, Any>, ObservableValue<out Any>> {

        override fun call(param: TableColumn.CellDataFeatures<Int, Any>): ObservableValue<out Any>? {
            return when (index) {
                0 -> ReadOnlyIntegerWrapper(param.value)
                1 -> ReadOnlyStringWrapper(computeFilterName(param.value))
                else -> null
            }
        }
    }
}
