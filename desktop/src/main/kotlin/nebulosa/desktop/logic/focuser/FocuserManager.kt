package nebulosa.desktop.logic.focuser

import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.focuser.FocuserView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserMovingChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext

class FocuserManager(private val view: FocuserView) :
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
        view.title = "Filter Wheel · $name"
    }

    fun updateStatus() {
        val text = if (moving) "moving" else "idle"
        view.status = text
    }

    fun openINDIPanelControl() {
        INDIPanelControlWindow.open(value)
    }

    fun moveIn() {
        value?.moveFocusIn(view.increment)
    }

    fun moveOut() {
        value?.moveFocusOut(view.increment)
    }

    fun moveTo() {
        value?.moveFocusTo(view.absolute)
    }

    fun sync() {
        value?.syncFocusTo(view.absolute)
    }

    fun abort() {
        value?.abortFocus()
    }

    fun updateMaxIncrement() {
        view.maxIncrement = value?.maxPosition ?: 0
    }

    fun updateMaxAbsolute() {
        view.absoluteMax = value?.maxPosition ?: 0
    }

    fun savePreferences() {
        preferences.double("focuser.screen.x", view.x)
        preferences.double("focuser.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("focuser.screen.x")?.let { view.x = it }
        preferences.double("focuser.screen.y")?.let { view.y = it }
    }

    override fun close() {
        savePreferences()
    }
}
