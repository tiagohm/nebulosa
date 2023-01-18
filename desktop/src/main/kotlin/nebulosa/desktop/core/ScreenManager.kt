package nebulosa.desktop.core

import nebulosa.desktop.cameras.CameraManagerScreen
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.filterwheels.FilterWheelManagerScreen
import nebulosa.desktop.focusers.FocuserManagerScreen
import nebulosa.desktop.imageviewer.ImageViewerScreen
import nebulosa.desktop.indi.INDIPanelControlScreen
import nebulosa.desktop.mounts.MountManagerScreen
import nebulosa.desktop.platesolving.PlateSolverScreen
import nebulosa.indi.device.Device
import nebulosa.indi.device.cameras.Camera
import org.koin.core.component.KoinComponent
import java.io.File

class ScreenManager : KoinComponent {

    private val cameraManagerScreen by lazy { CameraManagerScreen() }
    private val mountManagerScreen by lazy { MountManagerScreen() }
    private val focuserManagerScreen by lazy { FocuserManagerScreen() }
    private val filterWheelManagerScreen by lazy { FilterWheelManagerScreen() }
    private val indiPanelControlScreen by lazy { INDIPanelControlScreen() }

    private val screens = HashSet<Screen>()

    fun openByName(
        name: String,
        requestFocus: Boolean = true,
        bringToFront: Boolean = true,
    ): Screen {
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

        screen.show(requestFocus, bringToFront)

        return screen
    }

    fun openINDIPanelControl(device: Device? = null): INDIPanelControlScreen {
        val screen = indiPanelControlScreen
        screen.show(bringToFront = true)
        if (device != null) screen.select(device)
        screens.add(screen)
        return screen
    }

    fun openImageViewer(file: File, camera: Camera? = null): ImageViewerScreen {
        val screen = screens
            .filterIsInstance<ImageViewerScreen>()
            .firstOrNull { if (camera == null) it.camera == null && !it.isShowing else it.camera === camera }
            ?: ImageViewerScreen(camera)

        screens.add(screen)

        screen.show()
        screen.open(file)

        return screen
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
