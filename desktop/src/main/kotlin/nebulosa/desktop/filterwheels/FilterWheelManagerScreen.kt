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
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import javafx.util.StringConverter
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.scene.control.ButtonValueFactory
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.filterwheels.FilterWheel
import org.koin.core.component.inject
import kotlin.math.max
import kotlin.math.min

class FilterWheelManagerScreen : Screen("FilterWheelManager", "nebulosa-fw-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var filterWheels: ChoiceBox<FilterWheel>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var openINDI: Button
    @FXML private lateinit var compactMode: CheckMenuItem
    @FXML private lateinit var useFilterWheelAsShutter: CheckBox
    @FXML private lateinit var filterAsShutter: ChoiceBox<String>
    @FXML private lateinit var filterSlots: TableView<Int>
    @FXML private lateinit var filterSlot: ChoiceBox<Int>
    @FXML private lateinit var moveToSelectedFilterSlot: Button

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
        equipmentManager.selectedFilterWheel.isConnected.on { connect.styleClass.toggle("text-red-700", "text-blue-grey-700") }

        openINDI.disableProperty().bind(connect.disableProperty())

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
            if (it < 0 || it >= filterWheel.slotCount) return@on
            preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter", it)
        }

        filterSlot.converter = FilterSlotStringConverter()
        filterSlot.disableProperty().bind(isNotConnectedOrMoving)

        moveToSelectedFilterSlot.disableProperty().bind(
            isNotConnectedOrMoving or filterSlot.selectionModel.selectedItemProperty()
                .isEqualTo(equipmentManager.selectedFilterWheel.position.asObject()) or filterSlot.selectionModel.selectedItemProperty().isNull
        )

        equipmentManager.selectedFilterWheel.on {
            updateTitle()
            updateFilterSlots()
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
        }

        equipmentManager.selectedFilterWheel.position.on {
            updateTitle()
            filterSlot.value = it
        }

        equipmentManager.selectedFilterWheel.slotCount.on {
            updateFilterSlots()
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
            updateScreenHeight()
        }

        equipmentManager.selectedFilterWheel.isConnected.on {
            updateFilterSlots()
            updateUseFilterWheelAsShutter()
            updateFilterAsShutter()
            updateScreenHeight()
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

        compactMode.isSelected = preferences.bool("filterWheelManager.screen.compactMode")

        updateCompactMode()
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
    private fun openMenu(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            menu.show(event.source as Node, event.screenX, event.screenY)
            event.consume()
        }
    }

    @FXML
    private fun openINDI() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
        screenManager.openINDIPanelControl(filterWheel)
    }

    @FXML
    private fun toggleUseFilterWheelAsShutter() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return
        preferences.bool("filterWheelManager.equipment.${filterWheel.name}.useFilterWheelAsShutter", useFilterWheelAsShutter.isSelected)
    }

    @FXML
    private fun toggleCompactMode() {
        preferences.bool("filterWheelManager.screen.compactMode", compactMode.isSelected)
        updateCompactMode()
    }

    @FXML
    private fun moveToSelectedFilterSlot() {
        val item = filterSlot.selectionModel.selectedItem ?: return
        equipmentManager.selectedFilterWheel.get().moveTo(item)
    }

    private fun computeFilterName(position: Int): String {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return "Filter #$position"
        val label = preferences.string("filterWheelManager.equipment.${filterWheel.name}.filterSlot.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    private fun updateFilterSlots() {
        val filterWheel = equipmentManager.selectedFilterWheel.get() ?: return

        if (filterWheel.isConnected && filterWheel.slotCount > 0) {
            filterSlots.items.setAll((1..filterWheel.slotCount).toList())
            filterSlot.items.setAll((1..filterWheel.slotCount).toList())
            filterSlot.value = filterWheel.position
        } else {
            filterSlots.items.clear()
            filterSlot.items.clear()
            filterSlot.value = null
        }
    }

    private fun updateScreenHeight() {
        val isCompactMode = preferences.bool("filterWheelManager.screen.compactMode")

        height = if (isCompactMode) {
            170.0
        } else {
            val filterWheel = equipmentManager.selectedFilterWheel.get()

            if (filterWheel != null && filterWheel.isConnected) {
                val slotCount = min(8, max(1, filterWheel.slotCount))
                161.0 + slotCount * 28.0
            } else {
                188.0
            }
        }
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

    private fun updateCompactMode() {
        val isCompactMode = preferences.bool("filterWheelManager.screen.compactMode")

        if (isCompactMode) {
            filterSlots.isVisible = false
            filterSlots.isManaged = false

            filterSlot.parent.isVisible = true
            filterSlot.parent.isManaged = true
        } else {
            filterSlots.isVisible = true
            filterSlots.isManaged = true

            filterSlot.parent.isVisible = false
            filterSlot.parent.isManaged = false
        }

        updateFilterSlots()
        updateScreenHeight()
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

    private inner class FilterSlotStringConverter : StringConverter<Int>() {

        override fun toString(slot: Int?) = slot?.let(::computeFilterName) ?: "No filter selected"

        override fun fromString(text: String?) = null
    }
}
