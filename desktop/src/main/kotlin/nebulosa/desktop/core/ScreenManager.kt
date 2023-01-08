package nebulosa.desktop.core

import nebulosa.desktop.cameras.CameraManagerScreen
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.filterwheels.FilterWheelManagerScreen
import nebulosa.desktop.focusers.FocuserManagerScreen
import nebulosa.desktop.indi.INDIPanelControlScreen
import nebulosa.desktop.mounts.MountManagerScreen
import nebulosa.desktop.platesolving.PlateSolverScreen
import nebulosa.indi.devices.Device
import org.koin.core.component.KoinComponent

class ScreenManager : KoinComponent {

    private val cameraManagerScreen by lazy { CameraManagerScreen() }
    private val mountManagerScreen by lazy { MountManagerScreen() }
    private val focuserManagerScreen by lazy { FocuserManagerScreen() }
    private val filterWheelManagerScreen by lazy { FilterWheelManagerScreen() }
    private val indiPanelControlScreen by lazy { INDIPanelControlScreen() }

    private val screens = HashSet<Screen>()

    fun openByName(name: String): Screen {
        val screen = when (name) {
            CAMERA -> cameraManagerScreen
            MOUNT -> mountManagerScreen
            FOCUSER -> focuserManagerScreen
            FILTER_WHEEL -> filterWheelManagerScreen
            PLATE_SOLVING -> PlateSolverScreen()
            INDI -> indiPanelControlScreen
            else -> throw IllegalArgumentException("unknown screen: $name")
        }

        screens.add(screen)

        screen.showAndFocus()

        return screen
    }

    fun openINDIPanelControl(device: Device? = null) {
        val screen = indiPanelControlScreen
        screen.showAndFocus()
        if (device != null) screen.select(device)
        screens.add(screen)
    }

    fun closeAll() {
        screens.forEach(Screen::close)
    }

    companion object {

        const val CAMERA = "CAMERA"
        const val MOUNT = "MOUNT"
        const val FOCUSER = "FOCUSER"
        const val FILTER_WHEEL = "FILTER_WHEEL"
        const val PLATE_SOLVING = "PLATE_SOLVING"
        const val INDI = "INDI"
    }
}
