package nebulosa.desktop.logic.focuser

import nebulosa.desktop.core.ScreenManager
import nebulosa.desktop.equipments.FocuserProperty
import nebulosa.desktop.gui.focuser.FocuserWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focusers.Focuser
import nebulosa.indi.device.focusers.FocuserMaxPositionChanged
import nebulosa.indi.device.focusers.FocuserMovingChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FocuserManager(private val window: FocuserWindow) : FocuserProperty(), KoinComponent {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()
    private val screenManager by inject<ScreenManager>()

    val focusers get() = equipmentManager.attachedFocusers

    override fun onChanged(prev: Focuser?, new: Focuser) {
        super.onChanged(prev, new)

        savePreferences(prev)
        updateTitle()
        updateMaxIncrement()
        updateMaxAbsolute()
        loadPreferences(new)

        equipmentManager.selectedFocuser.set(new)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>) {
        super.onDeviceEvent(event)

        when (event) {
            is FocuserMaxPositionChanged -> {
                updateMaxIncrement()
                updateMaxAbsolute()
            }
            is FocuserMovingChanged -> updateStatus()
        }
    }

    fun updateTitle() {
        window.title = "Filter Wheel · $name"
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
        INDIPanelControlWindow.open(value)
    }

    fun moveIn() {
        value?.moveFocusIn(window.increment)
    }

    fun moveOut() {
        value?.moveFocusOut(window.increment)
    }

    fun moveTo() {
        value?.moveFocusTo(window.absolute)
    }

    fun sync() {
        value?.syncFocusTo(window.absolute)
    }

    fun abort() {
        value?.abortFocus()
    }

    fun updateMaxIncrement() {
        window.maxIncrement = value?.maxPosition ?: 0
    }

    fun updateMaxAbsolute() {
        window.absoluteMax = value?.maxPosition ?: 0
    }

    fun savePreferences(device: Focuser? = value) {
        if (device == null) {
            preferences.double("focuser.screen.x", window.x)
            preferences.double("focuser.screen.y", window.y)
        }
    }

    fun loadPreferences(device: Focuser? = value) {
        if (device != null) {
        } else {
            preferences.double("focuser.screen.x")?.let { window.x = it }
            preferences.double("focuser.screen.y")?.let { window.y = it }
        }
    }

    override fun close() {
        super.close()

        savePreferences(null)
        savePreferences()
    }
}
