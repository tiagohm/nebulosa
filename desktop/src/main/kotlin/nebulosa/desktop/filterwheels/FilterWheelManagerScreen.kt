package nebulosa.desktop.filterwheels

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.onZero
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialColor
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.scene.control.ButtonValueFactory
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.filterwheels.FilterWheel
import org.koin.core.component.inject

class FilterWheelManagerScreen : Screen("FilterWheelManager", "nebulosa-fw-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var filterWheels: ChoiceBox<FilterWheel>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var useFilterWheelAsShutter: CheckBox
    @FXML private lateinit var filterAsShutter: ChoiceBox<String>
    @FXML private lateinit var filterSlots: TableView<Int>

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Filter Wheel"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedFilterWheel.isConnected.not()
        val isConnecting = equipmentManager.selectedFilterWheel.isConnecting
        val isMoving = equipmentManager.selectedFilterWheel.isMoving
        val isNotConnectedOrMoving = isNotConnected or isMoving

        filterWheels.converter = DeviceStringConverter()
        filterWheels.disableProperty().bind(isConnecting or isMoving)
        filterWheels.itemsProperty().bind(equipmentManager.attachedFilterWheels)
        equipmentManager.selectedFilterWheel.bind(filterWheels.selectionModel.selectedItemProperty())

        connect.disableProperty().bind(equipmentManager.selectedFilterWheel.isNull or isConnecting or isMoving)
        connect.textProperty().bind(equipmentManager.selectedFilterWheel.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        connect.textFillProperty().bind(equipmentManager.selectedFilterWheel.isConnected.between(MaterialColor.RED_700, MaterialColor.BLUE_GREY_700))

        filterSlots.disableProperty().bind(isNotConnectedOrMoving)
        useFilterWheelAsShutter.disableProperty().bind(isNotConnectedOrMoving)
        filterAsShutter.disableProperty().bind(isNotConnectedOrMoving or !useFilterWheelAsShutter.selectedProperty())

        filterSlots.columns[0].cellValueFactory = FilterSlotValueFactory(0)
        filterSlots.columns[1].cellFactory = TextFieldTableCell.forTableColumn()
        filterSlots.columns[1].cellValueFactory = FilterSlotValueFactory(1)

        filterSlots.columns[1].setOnEditCommit {
            val label = it.newValue as? String
            if (label.isNullOrBlank()) return@setOnEditCommit
            val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return@setOnEditCommit
            val position = it.tableView.items[it.tablePosition.row]
            preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label", label)
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        filterSlots.columns[2].cellFactory = object : ButtonValueFactory<Int, String> {

            override fun cell(item: Int, node: Node?): Node {
                val button = node as? Button

                node?.also(::dispose)

                return (button ?: Button("Move To")).apply {
                    cursor = Cursor.HAND
                    disableProperty().bind(equipmentManager.selectedFilterWheel.position.isEqualTo(item))
                    setOnAction { equipmentManager.selectedFilterWheel.get().moveTo(item) }
                }
            }

            override fun dispose(node: Node) {
                node as Button
                node.disableProperty().unbind()
                node.onAction = null
            }
        }

        filterAsShutter.selectionModel.selectedIndexProperty().on {
            val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return@on
            preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter", it)
        }

        equipmentManager.selectedFilterWheel.onZero {
            updateTitle()
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        equipmentManager.selectedFilterWheel.position.onZero(::updateTitle)

        equipmentManager.selectedFilterWheel.slotCount.on {
            filterSlots.items.setAll((1..it).toList())
            height = 180.0 + it * 29.9
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        equipmentManager.selectedFilterWheel.isConnected.on {
            if (it) {
                updateUseFilterWheelAsShutter()
                updateFilterAsShutter()
            }
        }

        preferences.double("filterWheelManager.screen.x")?.let { x = it }
        preferences.double("filterWheelManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("filterWheelManager.screen.x", it) }
        yProperty().on { preferences.double("filterWheelManager.screen.y", it) }
    }

    override fun onStart() {
        val filterWheel = equipmentManager.selectedFilterWheel.get()

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
        if (!equipmentManager.selectedFilterWheel.isConnected.get()) {
            equipmentManager.selectedFilterWheel.get().connect()
        } else {
            equipmentManager.selectedFilterWheel.get().disconnect()
        }
    }

    @FXML
    private fun toggleUseFilterWheelAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
        preferences.bool("filterWheelManager.equipment.${filterWheel.name}.useFilterWheelAsShutter", useFilterWheelAsShutter.isSelected)
    }

    private fun computeFilterName(position: Int): String {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return "Filter #$position"
        val label = preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    private fun updateTitle() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
        val position = equipmentManager.selectedFilterWheel.position.get()
        if (position < 0) return
        title = "Filter Wheel · ${filterWheel.name} · ${computeFilterName(position)}"
    }

    private fun updateUseFilterWheelAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
        useFilterWheelAsShutter.isSelected = preferences.bool("filterWheelManager.equipment.${filterWheel.name}.useFilterWheelAsShutter")
        val selectedFilterAsShutter = preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter") ?: 0
        filterAsShutter.items.setAll((1..filterWheel.slotCount).map(::computeFilterName))
        filterAsShutter.selectionModel.select(selectedFilterAsShutter)
    }

    private fun updateFilterAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
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
