package nebulosa.desktop.logic.focuser

import nebulosa.desktop.helper.runBlockingMain
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.task.TaskExecutor
import nebulosa.desktop.view.focuser.FocuserView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserMaxPositionChanged
import nebulosa.indi.device.focuser.FocuserMovingChanged
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class FocuserManager(
    @Autowired internal val view: FocuserView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : AbstractManager(), FocuserProperty by equipmentManager.selectedFocuser {

    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView
    @Autowired private lateinit var taskExecutor: TaskExecutor

    val focusers
        get() = equipmentManager.attachedFocusers

    fun initialize() {
        registerListener(this)
    }

    override fun onChanged(prev: Focuser?, device: Focuser) {
        if (prev !== device) savePreferences()

        launch { updateTitle() }
        launch { updateMaxIncrement() }
        launch { updateAbsoluteMax() }
        launch { loadPreferences() }
    }

    override fun onReset() = Unit

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Focuser) = runBlockingMain {
        when (event) {
            is FocuserMovingChanged -> updateStatus()
            is FocuserMaxPositionChanged -> {
                updateMaxIncrement()
                updateAbsoluteMax()
            }
        }
    }

    private suspend fun updateTitle() = withMain {
        view.title = "Focuser · $name"
    }

    private suspend fun updateStatus() {
        view.updateStatus(if (moving) "moving" else "idle")
    }

    fun openINDIPanelControl() {
        indiPanelControlView.show(value)
    }

    fun moveIn() {
        val task = FocuserRelativeMoveTask(value, view.increment, FocuserDirection.IN)
        taskExecutor.execute(task)
    }

    fun moveOut() {
        val task = FocuserRelativeMoveTask(value, view.increment, FocuserDirection.OUT)
        taskExecutor.execute(task)
    }

    fun moveTo() {
        val task = FocuserAbsoluteMoveTask(value, view.absolute)
        taskExecutor.execute(task)
    }

    fun sync() {
        value?.syncFocusTo(view.absolute)
    }

    fun abort() {
        value?.abortFocus()
    }

    private suspend fun updateMaxIncrement() {
        view.updateMaxIncrement(value?.maxPosition ?: 0)
    }

    private suspend fun updateAbsoluteMax() {
        view.updateAbsoluteMax(value?.maxPosition ?: 0)
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferences.double("focuser.screen.x", max(0.0, view.x))
        preferences.double("focuser.screen.y", max(0.0, view.y))
    }

    fun loadPreferences() {
        preferences.double("focuser.screen.x")?.let { view.x = it }
        preferences.double("focuser.screen.y")?.let { view.y = it }
    }

    override fun close() {
        savePreferences()
    }
}
