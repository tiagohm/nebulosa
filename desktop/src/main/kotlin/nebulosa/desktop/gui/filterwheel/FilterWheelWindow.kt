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
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.control.ButtonValueFactory
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.filterwheel.FilterWheelManager
import nebulosa.indi.device.filterwheels.FilterWheel
import kotlin.math.max
import kotlin.math.min

class FilterWheelWindow : AbstractWindow() {

    override val resourceName = "FilterWheel"

    override val icon = "nebulosa-filterwheel"

    @FXML private lateinit var filterWheelChoiceBox: ChoiceBox<FilterWheel>
    @FXML private lateinit var connectButton: Button
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var compactModeMenuItem: CheckMenuItem
    @FXML private lateinit var useFilterWheelAsShutterCheckBox: CheckBox
    @FXML private lateinit var filterAsShutterChoiceBox: ChoiceBox<String>
    @FXML private lateinit var filterSlotTableView: TableView<Int>
    @FXML private lateinit var filterSlotChoiceBox: ChoiceBox<Int>
    @FXML private lateinit var moveToSelectedFilterButton: Button

    private val filterWheelManager = FilterWheelManager(this)

    init {
        title = "FilterWheel"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = filterWheelManager.isConnected.not()
        val isConnecting = filterWheelManager.isConnecting
        val isMoving = filterWheelManager.isMoving
        val isNotConnectedOrMoving = isNotConnected or isMoving

        filterWheelChoiceBox.converter = DeviceStringConverter()
        filterWheelChoiceBox.disableProperty().bind(isConnecting or isMoving)
        filterWheelChoiceBox.itemsProperty().bind(filterWheelManager.filterWheels)
        filterWheelManager.bind(filterWheelChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(filterWheelManager.isNull or isConnecting or isMoving)
        connectButton.textProperty().bind(filterWheelManager.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        filterWheelManager.isConnected.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700") }

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
                    disableProperty().bind(filterWheelManager.position.isEqualTo(item))
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
                .isEqualTo(filterWheelManager.position.asObject()) or filterSlotChoiceBox.selectionModel.selectedItemProperty().isNull
        )

        filterWheelManager.loadPreferences(null)

        xProperty().on { filterWheelManager.saveScreenLocation(it, y) }
        yProperty().on { filterWheelManager.saveScreenLocation(x, it) }
    }

    override fun onStart() {
        filterWheelManager.loadPreferences()
    }

    var status
        get() = "" // statusLabel.text
        set(value) {
            // statusLabel.text = value
        }

    var isCompactMode
        get() = compactModeMenuItem.isSelected
        set(value) {
            compactModeMenuItem.isSelected = value

            filterSlotTableView.isVisible = !value
            filterSlotTableView.isManaged = !value

            filterSlotChoiceBox.parent.isVisible = value
            filterSlotChoiceBox.parent.isManaged = value

            updateScreenHeight()
        }

    var isUseFilterWheelAsShutter
        get() = useFilterWheelAsShutterCheckBox.isSelected
        set(value) {
            useFilterWheelAsShutterCheckBox.isSelected = value
        }

    var filterAsShutter
        get() = filterAsShutterChoiceBox.selectionModel.selectedIndex + 1
        set(value) {
            filterAsShutterChoiceBox.selectionModel.select(value - 1)
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
    private fun openINDI() {
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
        height = if (isCompactMode) {
            170.0
        } else {
            if (filterWheelManager.isConnected.get()) {
                val slotCount = min(8, max(1, filterWheelManager.slotCount.get()))
                161.0 + slotCount * 28.0
            } else {
                188.0
            }
        }
    }

    fun updateFilterNames(
        names: List<String>,
        selectedFilterAsShutter: Int,
        position: Int,
    ) {
        if (filterWheelManager.isConnected.get()) {
            filterAsShutterChoiceBox.items.setAll(names)
            filterAsShutter = selectedFilterAsShutter

            val positions = (1..names.size).toList()
            filterSlotTableView.items.setAll(positions)

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

    private inner class FilterSlotStringConverter : StringConverter<Int>() {

        override fun toString(slot: Int?) = slot?.let(filterWheelManager::computeFilterName) ?: "No filter selected"

        override fun fromString(text: String?) = null
    }

    companion object {

        @Volatile private var window: FilterWheelWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = FilterWheelWindow()
            window!!.open(bringToFront = true)
        }
    }
}
