package nebulosa.desktop.logic.home

import javafx.stage.FileChooser
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.connection.ConnectionManager
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.camera.CameraView
import nebulosa.desktop.view.filterwheel.FilterWheelView
import nebulosa.desktop.view.focuser.FocuserView
import nebulosa.desktop.view.framing.FramingView
import nebulosa.desktop.view.home.HomeView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.desktop.view.mount.MountView
import nebulosa.desktop.view.platesolver.PlateSolverView
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.Closeable
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

class HomeManager(private val view: HomeView) : Closeable {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var connectionManager: ConnectionManager
    @Autowired private lateinit var appDirectory: Path

    @Autowired private lateinit var cameraView: CameraView
    @Autowired private lateinit var mountView: MountView
    @Autowired private lateinit var focuserView: FocuserView
    @Autowired private lateinit var filterWheelView: FilterWheelView
    @Autowired private lateinit var atlasView: AtlasView
    @Autowired private lateinit var plateSolverView: PlateSolverView
    @Autowired private lateinit var imageWindowOpener: ImageWindow.Opener
    @Autowired private lateinit var framingView: FramingView
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    val connectedProperty
        get() = equipmentManager.connectedProperty

    fun connect() {
        if (!connectionManager.isConnected()) {
            val host = view.host
            val port = view.port

            try {
                connectionManager.connect(host, port)
            } catch (e: Throwable) {
                LOG.error("connection failed", e)

                return view.showAlert(
                    "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                    "Connection failed"
                )
            }

            preferences.string("connection.host", host)
            preferences.int("connection.port", port)
        } else {
            connectionManager.disconnect()
        }
    }

    fun open(name: String) {
        when (name) {
            "NEW_IMAGE" -> openNewImage()
            "CAMERA" -> cameraView.show(bringToFront = true)
            "MOUNT" -> mountView.show(bringToFront = true)
            "FOCUSER" -> focuserView.show(bringToFront = true)
            "FILTER_WHEEL" -> filterWheelView.show(bringToFront = true)
            "ATLAS" -> atlasView.show(bringToFront = true)
            "PLATE_SOLVER" -> plateSolverView.show(bringToFront = true)
            "FRAMING" -> framingView.show(bringToFront = true)
            "INDI" -> indiPanelControlView.show(bringToFront = true)
        }
    }

    private fun openNewImage() {
        val initialDirectoryPath = preferences
            .string("home.newImage.initialDirectory")
            ?.let(::Path)?.takeIf { it.exists() }
            ?: appDirectory

        val file = with(FileChooser()) {
            title = "Open New Image"
            initialDirectory = initialDirectoryPath.toFile()
            extensionFilters.add(FileChooser.ExtensionFilter("All Image Files", "*.fits", "*.fit", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
            extensionFilters.add(FileChooser.ExtensionFilter("Extended Image Files", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            showOpenDialog(null) ?: return
        }

        preferences.string("home.newImage.initialDirectory", file.parent)

        try {
            imageWindowOpener.open(file)
        } catch (e: Throwable) {
            LOG.error("image load error", e)

            view.showAlert("Unable to load this image.", "Image Error")
        }
    }

    fun savePreferences() {
        preferences.double("home.screen.x", view.x)
        preferences.double("home.screen.y", view.y)
    }

    fun loadPreferences() {
        view.host = preferences.string("connection.host") ?: ""
        view.port = preferences.int("connection.port") ?: 7624

        preferences.double("home.screen.x")?.let { view.x = it }
        preferences.double("home.screen.y")?.let { view.y = it }
    }

    override fun close() {
        savePreferences()

        equipmentManager.close()
        connectionManager.disconnect()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HomeManager::class.java)
    }
}
