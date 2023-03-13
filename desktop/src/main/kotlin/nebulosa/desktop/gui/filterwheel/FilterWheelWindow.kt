package nebulosa.desktop.gui.filterwheel

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
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.ButtonValueFactory
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.filterwheel.FilterWheelManager
import nebulosa.desktop.logic.isNull
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.filterwheel.FilterWheelView
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min

@Component
class FilterWheelWindow : AbstractWindow("FilterWheel", "rgb"), FilterWheelView {

    @Lazy @Autowired private lateinit var filterWheelManager: FilterWheelManager

    @FXML private lateinit var filterWheelChoiceBox: ChoiceBox<FilterWheel>
    @FXML private lateinit var connectButton: TwoStateButton
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var compactModeMenuItem: CheckMenuItem
    @FXML private lateinit var useFilterWheelAsShutterCheckBox: CheckBox
    @FXML private lateinit var filterAsShutterChoiceBox: ChoiceBox<String>
    @FXML private lateinit var filterSlotTableView: TableView<Int>
    @FXML private lateinit var filterSlotChoiceBox: ChoiceBox<Int>
    @FXML private lateinit var moveToSelectedFilterButton: Button

    init {
        title = "Filter Wheel"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = filterWheelManager.connectedProperty.not()
        val isConnecting = filterWheelManager.connectingProperty
        val isMoving = filterWheelManager.movingProperty
        val isNotConnectedOrMoving = isNotConnected or isMoving

        filterWheelManager.initialize()

        filterWheelChoiceBox.converter = FilterWheelStringConverter
        filterWheelChoiceBox.disableProperty().bind(isConnecting or isMoving)
        filterWheelChoiceBox.itemsProperty().bind(filterWheelManager.filterWheels)
        filterWheelManager.bind(filterWheelChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(filterWheelManager.isNull() or isConnecting or isMoving)
        filterWheelManager.connectedProperty.on { connectButton.state = it }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        useFilterWheelAsShutterCheckBox.disableProperty().bind(isNotConnectedOrMoving)
        filterAsShutterChoiceBox.disableProperty().bind(isNotConnectedOrMoving or !useFilterWheelAsShutterCheckBox.selectedProperty())

        filterSlotTableView.disableProperty().bind(isNotConnectedOrMoving)
        filterSlotTableView.columns[0].cellValueFactory = FilterSlotValueFactory(0)
        filterSlotTableView.columns[1].cellFactory = TextFieldTableCell.forTableColumn()
        filterSlotTableView.columns[1].cellValueFactory = FilterSlotValueFactory(1)
        filterSlotTableView.columns[1].setOnEditCommit {
            val label = it.newValue as? String ?: return@setOnEditCommit
            val position = it.tableView.items[it.tablePosition.row]
            filterWheelManager.updateFilterName(position, label)
        }

        filterSlotTableView.columns[2].cellFactory = object : ButtonValueFactory<Int, String> {

            override fun cell(item: Int, node: Node?): Node {
                val button = node as? Button

                node?.also(::dispose)

                return (button ?: Button("Move To")).apply {
                    cursor = Cursor.HAND
                    disableProperty().bind(filterWheelManager.positionProperty.isEqualTo(item))
                    setOnAction { filterWheelManager.moveTo(item) }
                }
            }

            override fun dispose(node: Node) {
                node as Button
                node.disableProperty().unbind()
                node.onAction = null
            }
        }

        filterAsShutterChoiceBox.selectionModel.selectedIndexProperty()
            .on { filterWheelManager.updateFilterAsShutter(it + 1) }

        filterSlotChoiceBox.converter = FilterSlotStringConverter()
        filterSlotChoiceBox.disableProperty().bind(isNotConnectedOrMoving)

        moveToSelectedFilterButton.disableProperty().bind(
            isNotConnectedOrMoving or filterSlotChoiceBox.selectionModel.selectedItemProperty()
                .isEqualTo(filterWheelManager.positionProperty.asObject()) or filterSlotChoiceBox.selectionModel.selectedItemProperty().isNull
        )

        filterWheelManager.loadPreferences(null)
    }

    override fun onStart() {
        filterWheelManager.loadPreferences()
    }

    override fun onStop() {
        filterWheelManager.savePreferences()
    }

    override val status
        get() = "" // statusLabel.text

    override val compactMode
        get() = compactModeMenuItem.isSelected

    override val useFilterWheelAsShutter
        get() = useFilterWheelAsShutterCheckBox.isSelected

    override val filterAsShutter
        get() = filterAsShutterChoiceBox.selectionModel.selectedIndex + 1

    override fun updateStatus(status: String) = Unit

    override fun useCompactMode(enable: Boolean) {
        compactModeMenuItem.isSelected = enable

        filterSlotTableView.isVisible = !enable
        filterSlotTableView.isManaged = !enable

        filterSlotChoiceBox.parent.isVisible = enable
        filterSlotChoiceBox.parent.isManaged = enable

        updateScreenHeight()
    }

    @FXML
    private fun connect() {
        filterWheelManager.connect()
    }

    @FXML
    private fun openMenu(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            menu.show(event.source as Node, event.screenX, event.screenY)
            event.consume()
        }
    }

    @FXML
    private fun openINDIPanelControl() {
        filterWheelManager.openINDIPanelControl()
    }

    @FXML
    private fun toggleUseFilterWheelAsShutter() {
        filterWheelManager.toggleUseFilterWheelAsShutter(useFilterWheelAsShutterCheckBox.isSelected)
    }

    @FXML
    private fun toggleCompactMode() {
        filterWheelManager.toggleCompactMode(compactModeMenuItem.isSelected)
    }

    @FXML
    private fun moveToSelectedFilter() {
        val item = filterSlotChoiceBox.selectionModel.selectedItem ?: return
        filterWheelManager.moveTo(item)
    }

    fun updateScreenHeight() {
        height = if (compactMode) {
            170.0
        } else {
            if (filterWheelManager.connected) {
                val slotCount = min(8, max(1, filterWheelManager.count))
                161.0 + slotCount * 28.0
            } else {
                188.0
            }
        }
    }

    override fun updateFilterNames(
        names: List<String>,
        useFilterWheelAsShutter: Boolean,
        filterAsShutter: Int,
        position: Int,
    ) {
        useFilterWheelAsShutterCheckBox.isSelected = useFilterWheelAsShutter

        if (filterWheelManager.connected) {
            filterAsShutterChoiceBox.items.setAll(names)
            filterAsShutterChoiceBox.selectionModel.select(filterAsShutter - 1)

            val positions = (1..names.size).toList()
            filterSlotTableView.items.setAll(positions)

            filterSlotChoiceBox.value = null
            filterSlotChoiceBox.items.setAll(positions)
            filterSlotChoiceBox.value = position
        } else {
            filterAsShutterChoiceBox.items.clear()
            filterAsShutterChoiceBox.value = null

            filterSlotTableView.items.clear()

            filterSlotChoiceBox.items.clear()
            filterSlotChoiceBox.value = null
        }

        updateScreenHeight()
    }

    private inner class FilterSlotValueFactory(val index: Int) : Callback<TableColumn.CellDataFeatures<Int, Any>, ObservableValue<out Any>> {

        override fun call(param: TableColumn.CellDataFeatures<Int, Any>): ObservableValue<out Any>? {
            return when (index) {
                0 -> ReadOnlyIntegerWrapper(param.value)
                1 -> ReadOnlyStringWrapper(filterWheelManager.computeFilterName(param.value))
                else -> null
            }
        }
    }

    private object FilterWheelStringConverter : StringConverter<FilterWheel>() {

        override fun toString(device: FilterWheel?) = device?.name ?: "No filter wheel selected"

        override fun fromString(text: String?) = null
    }

    private inner class FilterSlotStringConverter : StringConverter<Int>() {

        override fun toString(slot: Int?) = slot?.let(filterWheelManager::computeFilterName) ?: "No filter selected"

        override fun fromString(text: String?) = null
    }
}
