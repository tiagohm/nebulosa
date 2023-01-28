package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.gui.filterwheel.FilterWheelWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheels.FilterWheel
import nebulosa.indi.device.filterwheels.FilterWheelCountChanged
import nebulosa.indi.device.filterwheels.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheels.FilterWheelPositionChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext

class FilterWheelManager(private val window: FilterWheelWindow) :
    FilterWheelProperty by GlobalContext.get().get<EquipmentManager>().selectedFilterWheel, KoinComponent {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()

    val filterWheels get() = equipmentManager.attachedFilterWheels

    init {
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
        window.title = "Filter Wheel · $name · $filterName"
    }

    fun updateStatus() {
        window.status = if (moving) "moving"
        else "idle"
    }

    fun openINDIPanelControl() {
        INDIPanelControlWindow.open(value)
    }

    fun toggleUseFilterWheelAsShutter(enable: Boolean) {
        preferences.bool("filterWheel.$name.useFilterWheelAsShutter", enable)
        window.isUseFilterWheelAsShutter = enable
    }

    fun updateFilterAsShutter(position: Int) {
        if (position !in 1..count) return
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
        window.updateFilterNames(filterNames, selectedFilterAsShutter, position)
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

    fun savePreferences() {
        preferences.double("filterWheel.screen.x")?.let { window.x = it }
        preferences.double("filterWheel.screen.y")?.let { window.y = it }
    }

    fun loadPreferences(device: FilterWheel? = value) {
        if (device != null) {
            updateFilterNames()
            syncFilterNames()

            window.isUseFilterWheelAsShutter = preferences.bool("filterWheel.${device.name}.useFilterWheelAsShutter")
        }

        window.isCompactMode = preferences.bool("filterWheel.compactMode")

        preferences.double("filterWheel.screen.x")?.let { window.x = it }
        preferences.double("filterWheel.screen.y")?.let { window.y = it }
    }

    override fun close() {
        unregisterListener(this)

        savePreferences()
    }
}
