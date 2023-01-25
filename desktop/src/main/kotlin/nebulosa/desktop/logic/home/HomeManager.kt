package nebulosa.desktop.logic.home

import javafx.stage.FileChooser
import nebulosa.desktop.gui.AbstractWindow.Companion.showAlert
import nebulosa.desktop.gui.camera.CameraWindow
import nebulosa.desktop.gui.filterwheel.FilterWheelWindow
import nebulosa.desktop.gui.focuser.FocuserWindow
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.connection.ConnectionManager
import nebulosa.desktop.preferences.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.Closeable
import kotlin.io.path.Path
import kotlin.io.path.exists

class HomeManager(private val window: HomeWindow) : KoinComponent, Closeable {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()
    private val connectionManager by inject<ConnectionManager>()

    val isConnected = equipmentManager.isConnected

    fun connect() {
        if (!connectionManager.isConnected()) {
            val host = window.host
            val port = window.port

            connectionManager.connect(host, port)

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
            "FOCUSER" -> FocuserWindow.open()
            "FILTER_WHEEL" -> FilterWheelWindow.open()
            "INDI" -> INDIPanelControlWindow.open()
        }
    }

    private fun openNewImage() {
        val initialDirectory = preferences
            .string("home.newImage.initialDirectory")
            ?.let(::Path)?.takeIf { it.exists() }
            ?: get(named("app"))

        val file = with(FileChooser()) {
            title = "Open New Image"
            this.initialDirectory = initialDirectory.toFile()
            extensionFilters.add(FileChooser.ExtensionFilter("All Image Files", "*.fits", "*.fit", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
            extensionFilters.add(FileChooser.ExtensionFilter("Extended Image Files", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            showOpenDialog(window) ?: return
        }

        preferences.string("home.newImage.initialDirectory", file.parent)

        try {
            ImageWindow.open(file)
        } catch (e: Throwable) {
            e.printStackTrace()
            showAlert("Unable to load this image.", "Image Error")
        }
    }

    fun savePreferences() {
        preferences.double("home.screen.x", window.x)
        preferences.double("home.screen.y", window.y)
    }

    fun loadPreferences() {
        window.host = preferences.string("connection.host") ?: ""
        window.port = preferences.int("connection.port") ?: 7624

        preferences.double("home.screen.x")?.let { window.x = it }
        preferences.double("home.screen.y")?.let { window.y = it }
    }

    override fun close() {
        savePreferences()

        equipmentManager.close()
        connectionManager.disconnect()
    }
}
