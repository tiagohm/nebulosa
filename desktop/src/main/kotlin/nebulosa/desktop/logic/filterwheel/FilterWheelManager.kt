package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.task.TaskExecutor
import nebulosa.desktop.view.filterwheel.FilterWheelView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelCountChanged
import nebulosa.indi.device.filterwheel.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FilterWheelManager(
    @Autowired private val view: FilterWheelView,
    @Autowired private val equipmentManager: EquipmentManager,
) : FilterWheelProperty by equipmentManager.selectedFilterWheel {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var indiPanelControlWindow: INDIPanelControlWindow
    @Autowired private lateinit var taskExecutor: TaskExecutor

    val filterWheels
        get() = equipmentManager.attachedFilterWheels

    fun initialize() {
        registerListener(this)
    }

    override fun onChanged(prev: FilterWheel?, device: FilterWheel) {
        if (prev !== device) savePreferences()

        updateTitle()

        loadPreferences(device)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: FilterWheel) {
        when (event) {
            is FilterWheelPositionChanged -> updateTitle()
            is FilterWheelCountChanged -> updateFilterNames()
            is FilterWheelMovingChanged -> updateStatus()
        }
    }

    val filterNames
        get() = (1..count).map(::computeFilterName)

    fun updateTitle() {
        val filterName = computeFilterName(position)
        view.title = "Filter Wheel · $name · $filterName"
    }

    fun updateStatus() {
        view.updateStatus(if (moving) "moving" else "idle")
    }

    fun openINDIPanelControl() {
        indiPanelControlWindow.show(bringToFront = true)
        indiPanelControlWindow.device = value
    }

    fun toggleUseFilterWheelAsShutter(enable: Boolean) {
        preferences.bool("filterWheel.$name.useFilterWheelAsShutter", enable)
    }

    fun updateFilterAsShutter(position: Int) {
        if (position !in 1..count) return
        preferences.int("filterWheel.$name.filterAsShutter", position)
    }

    fun toggleCompactMode(enable: Boolean) {
        preferences.bool("filterWheel.compactMode", enable)
        view.useCompactMode(enable)
    }

    fun updateFilterName(position: Int, label: String) {
        if (label.isBlank()) return
        preferences.string("filterWheel.$name.filter.$position.label", label)
        updateFilterNames()
        syncFilterNames()
    }

    fun updateFilterNames() {
        val useFilterWheelAsShutter = preferences.bool("filterWheel.$name.useFilterWheelAsShutter")
        val filterAsShutter = preferences.int("filterWheel.$name.filterAsShutter") ?: 1
        view.updateFilterNames(filterNames, useFilterWheelAsShutter, filterAsShutter, position)
        updateTitle()
    }

    fun moveTo(position: Int) {
        val task = FilterWheelMoveTask(value ?: return, position)
        taskExecutor.execute(task)
    }

    fun computeFilterName(position: Int): String {
        val label = preferences.string("filterWheel.$name.filter.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    fun syncFilterNames() {
        value?.filterNames(filterNames)
    }

    fun savePreferences() {
        preferences.double("filterWheel.screen.x", view.x)
        preferences.double("filterWheel.screen.y", view.y)
    }

    fun loadPreferences(device: FilterWheel? = value) {
        if (device != null) {
            updateFilterNames()
            syncFilterNames()
        }

        view.useCompactMode(preferences.bool("filterWheel.compactMode"))

        preferences.double("filterWheel.screen.x")?.let { view.x = it }
        preferences.double("filterWheel.screen.y")?.let { view.y = it }
    }

    override fun close() {
        unregisterListener(this)

        savePreferences()
    }
}
