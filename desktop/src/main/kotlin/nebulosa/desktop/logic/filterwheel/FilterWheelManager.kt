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

    override fun changed(prev: FilterWheel?, new: FilterWheel) {
        super.changed(prev, new)

        // savePreferences(prev)
        updateTitle()
        loadPreferences(new)

        equipmentManager.selectedFilterWheel.set(new)
    }

    override fun accept(event: DeviceEvent<FilterWheel>) {
        super.accept(event)

        when (event) {
            is FilterWheelPositionChanged -> updateTitle()
            is FilterWheelSlotCountChanged -> updateFilterNames()
            is FilterWheelMovingChanged -> updateStatus()
        }
    }

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
    }

    fun updateFilterAsShutter(position: Int) {
        if (position !in 1..slotCount.get()) return
        preferences.int("filterWheel.$name.filterAsShutter", position)
    }

    fun toggleCompactMode(enable: Boolean) {
        preferences.bool("filterWheel.compactMode", enable)
        window.isCompactMode = enable
    }

    fun updateFilterName(position: Int, label: String) {
        if (label.isBlank()) return
        preferences.string("filterWheel.$name.filter.$position.label", label)
        updateFilterNames()
    }

    fun updateFilterNames() {
        val names = (1..slotCount.get()).map(::computeFilterName)
        val selectedFilterAsShutter = preferences.int("filterWheel.$name.filterAsShutter") ?: 1
        window.updateFilterNames(names, selectedFilterAsShutter)
    }

    fun moveTo(position: Int) {
        value?.moveTo(position)
    }

    fun computeFilterName(position: Int): String {
        val label = preferences.string("filterWheel.$name.filter.$position.label") ?: ""
        return label.ifEmpty { "Filter #$position" }
    }

    fun loadPreferences(filterWheel: FilterWheel? = value) {
        if (filterWheel != null) {
            updateFilterNames()

            window.useFilterWheelAsShutter = preferences.bool("filterWheel.$name.useFilterWheelAsShutter")
        } else {
            window.isCompactMode = preferences.bool("filterWheel.compactMode")
            preferences.double("filterWheel.screen.x")?.let { window.x = it }
            preferences.double("filterWheel.screen.y")?.let { window.y = it }
        }
    }
}
