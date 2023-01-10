package nebulosa.desktop.home

import io.reactivex.rxjava3.disposables.Disposable
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import nebulosa.desktop.connections.ConnectionManager
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.imageviewer.ImageViewerScreen
import nebulosa.desktop.telescopecontrol.TelescopeControlManager
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class HomeScreen : Screen("Home") {

    private val connectionManager by inject<ConnectionManager>()
    private val equipmentManager by inject<EquipmentManager>()
    private val telescopeControlManager by inject<TelescopeControlManager>()

    @FXML private lateinit var host: TextField
    @FXML private lateinit var port: TextField
    @FXML private lateinit var connect: Button
    @FXML private lateinit var cameras: Button
    @FXML private lateinit var mounts: Button
    @FXML private lateinit var guiders: Button
    @FXML private lateinit var filterWheels: Button
    @FXML private lateinit var focusers: Button
    @FXML private lateinit var domes: Button
    @FXML private lateinit var rotators: Button
    @FXML private lateinit var switches: Button
    @FXML private lateinit var atlas: Button
    @FXML private lateinit var plateSolving: Button
    @FXML private lateinit var alignment: Button
    @FXML private lateinit var sequencer: Button
    @FXML private lateinit var imageViewer: Button
    @FXML private lateinit var indi: Button

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Nebulosa"
        isResizable = false
    }

    override fun onCreate() {
        host.disableProperty().bind(equipmentManager.connected)
        port.disableProperty().bind(equipmentManager.connected)
        cameras.disableProperty().bind(!equipmentManager.connected)
        mounts.disableProperty().bind(!equipmentManager.connected)
        guiders.disableProperty().bind(!equipmentManager.connected)
        filterWheels.disableProperty().bind(!equipmentManager.connected)
        focusers.disableProperty().bind(!equipmentManager.connected)
        domes.disableProperty().bind(!equipmentManager.connected)
        rotators.disableProperty().bind(!equipmentManager.connected)
        switches.disableProperty().bind(!equipmentManager.connected)
        alignment.disableProperty().bind(!equipmentManager.connected)
        sequencer.disableProperty().bind(!equipmentManager.connected)
        indi.disableProperty().bind(!equipmentManager.connected)

        connect.textProperty().bind(equipmentManager.connected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        equipmentManager.connected.on { connect.styleClass.toggle("text-blue-grey-700", "text-red-700") }

        host.text = preferences.string("connection.last.host") ?: ""
        port.text = preferences.string("connection.last.port") ?: ""

        preferences.double("home.screen.x")?.let { x = it }
        preferences.double("home.screen.y")?.let { y = it }

        xProperty().on { preferences.double("home.screen.x", it) }
        yProperty().on { preferences.double("home.screen.y", it) }
    }

    override fun onStart() {}

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null

        connectionManager.disconnect()

        screenManager.closeAll()
        ImageViewerScreen.close()

        telescopeControlManager.stopAll()
    }

    @FXML
    @Synchronized
    private fun connect() {
        if (!connectionManager.isConnected()) {
            try {
                val host = host.text.trim().ifEmpty { "localhost" }
                val port = port.text.trim().toIntOrNull() ?: 7624

                connectionManager.connect(host, port)

                preferences.string("connection.last.host", host)
                preferences.int("connection.last.port", port)
            } catch (e: Throwable) {
                showAlert(
                    "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                    "Connection failed"
                )
            }
        } else {
            connectionManager.disconnect()
        }
    }

    @FXML
    @Synchronized
    private fun open(event: ActionEvent) {
        when (val name = (event.source as Node).userData as String) {
            "NEW_IMAGE" -> openNewImage()
            else -> screenManager.openByName(name)
        }
    }

    private fun openNewImage() {
        val chooser = FileChooser()
        chooser.title = "Open New Image"
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("All Image Files", "*.fits", "*.fit", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Extended Image Files", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
        val file = chooser.showOpenDialog(this) ?: return

        try {
            screenManager.openImageViewer(file)
        } catch (e: Throwable) {
            LOG.error("image read error", e)
            showAlert("Unable to load this image.", "Image Error")
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HomeScreen::class.java)
    }
}
