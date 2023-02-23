package nebulosa.desktop.logic.focuser

import nebulosa.desktop.App
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.focuser.FocuserView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserMovingChanged
import org.springframework.beans.factory.annotation.Autowired

class FocuserManager(private val view: FocuserView) :
    FocuserProperty by App.beanFor<EquipmentManager>().selectedFocuser {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var equipmentManager: EquipmentManager

    val focusers
        get() = equipmentManager.attachedFocusers

    init {
        App.autowireBean(this)

        registerListener(this)
    }

    override fun onChanged(prev: Focuser?, device: Focuser) {
        if (prev !== device) savePreferences()

        updateTitle()
        updateMaxIncrement()
        updateAbsoluteMax()

        loadPreferences()
    }

    override fun onReset() = Unit

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Focuser) {
        when (event) {
            is FocuserMovingChanged -> updateStatus()
        }
    }

    private fun updateTitle() {
        view.title = "Filter Wheel · $name"
    }

    private fun updateStatus() {
        view.updateStatus(if (moving) "moving" else "idle")
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

    private fun updateMaxIncrement() {
        view.updateMaxIncrement(value?.maxPosition ?: 0)
    }

    private fun updateAbsoluteMax() {
        view.updateAbsoluteMax(value?.maxPosition ?: 0)
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
