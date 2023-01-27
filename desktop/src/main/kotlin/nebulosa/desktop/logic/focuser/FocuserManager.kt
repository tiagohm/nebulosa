package nebulosa.desktop.logic.focuser

import nebulosa.desktop.gui.focuser.FocuserWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focusers.Focuser
import nebulosa.indi.device.focusers.FocuserMovingChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext

class FocuserManager(private val window: FocuserWindow) :
    FocuserProperty by GlobalContext.get().get<EquipmentManager>().selectedFocuser, KoinComponent {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()

    @JvmField val focusers = equipmentManager.attachedFocusers

    init {
        registerListener(this)
    }

    override fun onChanged(prev: Focuser?, device: Focuser) {
        if (prev !== device) savePreferences()

        updateTitle()
        updateMaxIncrement()
        updateMaxAbsolute()

        loadPreferences()
    }

    override fun onReset() {}

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Focuser) {
        when (event) {
            is FocuserMovingChanged -> updateStatus()
        }
    }

    fun updateTitle() {
        window.title = "Filter Wheel · $name"
    }

    fun updateStatus() {
        val text = if (moving) "moving" else "idle"
        window.status = text
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

    fun savePreferences() {
        preferences.double("focuser.screen.x", window.x)
        preferences.double("focuser.screen.y", window.y)
    }

    fun loadPreferences() {
        preferences.double("focuser.screen.x")?.let { window.x = it }
        preferences.double("focuser.screen.y")?.let { window.y = it }
    }

    override fun close() {
        savePreferences()
    }
}
