package nebulosa.desktop.logic.home

import javafx.stage.FileChooser
import nebulosa.desktop.gui.atlas.AtlasWindow
import nebulosa.desktop.gui.camera.CameraWindow
import nebulosa.desktop.gui.filterwheel.FilterWheelWindow
import nebulosa.desktop.gui.focuser.FocuserWindow
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.gui.mount.MountWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.connection.ConnectionManager
import nebulosa.desktop.view.home.HomeView
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.Closeable
import kotlin.io.path.Path
import kotlin.io.path.exists

class HomeManager(private val view: HomeView) : KoinComponent, Closeable {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()
    private val connectionManager by inject<ConnectionManager>()

    val connected = equipmentManager.connectedProperty

    fun connect() {
        if (!connectionManager.isConnected()) {
            val host = view.host
            val port = view.port

            try {
                connectionManager.connect(host, port)
            } catch (e: Throwable) {
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
            "CAMERA" -> CameraWindow.open()
            "MOUNT" -> MountWindow.open()
            "FOCUSER" -> FocuserWindow.open()
            "FILTER_WHEEL" -> FilterWheelWindow.open()
            "ATLAS" -> AtlasWindow.open()
            "INDI" -> INDIPanelControlWindow.open()
        }
    }

    private fun openNewImage() {
        val initialDirectoryPath = preferences
            .string("home.newImage.initialDirectory")
            ?.let(::Path)?.takeIf { it.exists() }
            ?: get(named("app"))

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
            ImageWindow.open(file)
        } catch (e: Throwable) {
            e.printStackTrace()
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
}
