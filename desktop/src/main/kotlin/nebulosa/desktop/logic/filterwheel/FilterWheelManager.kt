package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.core.ScreenManager
import nebulosa.desktop.gui.filterwheel.FilterWheelWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheels.FilterWheel
import nebulosa.indi.device.filterwheels.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheels.FilterWheelPositionChanged
import nebulosa.indi.device.filterwheels.FilterWheelSlotCountChanged
import org.koin.core.component.inject

class FilterWheelManager(private val window: FilterWheelWindow) : FilterWheelProperty() {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()
    private val screenManager by inject<ScreenManager>()

    val filterWheels get() = equipmentManager.attachedFilterWheels

    override fun onChanged(prev: FilterWheel?, new: FilterWheel) {
        super.onChanged(prev, new)

        savePreferences(prev)
        updateTitle()
        loadPreferences(new)
        syncFilterNames()

        equipmentManager.selectedFilterWheel.set(new)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>) {
        super.onDeviceEvent(event)

        when (event) {
            is FilterWheelPositionChanged -> updateTitle()
            is FilterWheelSlotCountChanged -> updateFilterNames()
            is FilterWheelMovingChanged -> updateStatus()
        }
    }

    val filterNames
        get() = (1..slotCount.get()).map(::computeFilterName)

    fun updateTitle() {
        val filterName = computeFilterName(position.get())
        window.title = "Filter Wheel · $name · $filterName"
    }

    fun updateStatus() {
        val text = if (isMoving.get()) "moving" else "idle"
        window.status = text
    }

    fun connect() {
        if (isConnected.get()) value.disconnect()
        else value.connect()
    }

    fun openINDIPanelControl() {
        screenManager.openINDIPanelControl(value)
    }

    fun toggleUseFilterWheelAsShutter(enable: Boolean) {
        preferences.bool("filterWheel.$name.useFilterWheelAsShutter", enable)
        window.isUseFilterWheelAsShutter = enable
    }

    fun updateFilterAsShutter(position: Int) {
        if (position !in 1..slotCount.get()) return
        preferences.int("filterWheel.$name.filterAsShutter", position)
        window.filterAsShutter = position
    }

    fun toggleCompactMode(enable: Boolean) {
        preferences.bool("filterWheel.compactMode", enable)
        window.isCompactMode = enable
    }

    fun updateFilterName(position: Int, label: String) {
        if (label.isBlank()) return
        preferences.string("filterWheel.$name.filter.$position.label", label)
        updateFilterNames()
        syncFilterNames()
    }

    fun updateFilterNames() {
        val selectedFilterAsShutter = preferences.int("filterWheel.$name.filterAsShutter") ?: 1
        window.updateFilterNames(filterNames, selectedFilterAsShutter, position.get())
    }

    fun moveTo(position: Int) {
        value?.moveTo(position)
    }

    fun computeFilterName(position: Int): String {
        val label = preferences.string("filterWheel.$name.filter.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    fun syncFilterNames() {
        value?.filterNames(filterNames)
    }

    fun savePreferences(device: FilterWheel? = value) {
        if (device == null) {
            preferences.double("filterWheel.screen.x")?.let { window.x = it }
            preferences.double("filterWheel.screen.y")?.let { window.y = it }
        }
    }

    fun loadPreferences(device: FilterWheel? = value) {
        if (device != null) {
            updateFilterNames()

            window.isUseFilterWheelAsShutter = preferences.bool("filterWheel.${device.name}.useFilterWheelAsShutter")
        } else {
            window.isCompactMode = preferences.bool("filterWheel.compactMode")

            preferences.double("filterWheel.screen.x")?.let { window.x = it }
            preferences.double("filterWheel.screen.y")?.let { window.y = it }
        }
    }

    override fun close() {
        super.close()

        savePreferences(null)
        savePreferences()
    }
}
