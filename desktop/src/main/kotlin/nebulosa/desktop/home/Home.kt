package nebulosa.desktop.home

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import nebulosa.desktop.cameras.CameraManager
import nebulosa.desktop.cameras.ImageViewer
import nebulosa.desktop.connections.ConnectionService
import nebulosa.desktop.internal.Icon
import nebulosa.desktop.internal.MessageDialog
import nebulosa.desktop.internal.Screen
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraDetached
import org.koin.core.component.inject

class Home : Screen("Home") {

    private val connectionService by inject<ConnectionService>()
    private val camerasPage by inject<CameraManager>()

    @FXML private lateinit var connections: ChoiceBox<String>
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

    @JvmField val attachedCameras = ArrayList<Camera>(4)
    @JvmField val connected = SimpleBooleanProperty(false)
    @JvmField val imageViewers = HashSet<ImageViewer>(8)

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Nebulosa"
        isResizable = false

        connect.setOnAction { connect() }
        cameras.setOnAction { open("CAMERA") }
        mounts.setOnAction { open("MOUNT") }
        guiders.setOnAction { open("GUIDER") }
        filterWheels.setOnAction { open("FILTER_WHEEL") }
        focusers.setOnAction { open("FOCUSER") }
        domes.setOnAction { open("DOME") }
        rotators.setOnAction { open("ROTATOR") }
        switches.setOnAction { open("SWTICH") }
        atlas.setOnAction { open("ATLAS") }
        plateSolving.setOnAction { open("PLATE_SOLVING") }
        alignment.setOnAction { open("ALIGNMENT") }
        sequencer.setOnAction { open("SEQUENCER") }
        imageViewer.setOnAction { open("OPEN_NEW_IMAGE") }

        connections.disableProperty().bind(connected)
        host.disableProperty().bind(connected)
        port.disableProperty().bind(connected)
        cameras.disableProperty().bind(connected.not())
        mounts.disableProperty().bind(connected.not())
        guiders.disableProperty().bind(connected.not())
        filterWheels.disableProperty().bind(connected.not())
        focusers.disableProperty().bind(connected.not())
        domes.disableProperty().bind(connected.not())
        rotators.disableProperty().bind(connected.not())
        switches.disableProperty().bind(connected.not())
        alignment.disableProperty().bind(connected.not())
        sequencer.disableProperty().bind(connected.not())

        connected.addListener { _, _, value ->
            if (value) {
                connect.text = "Disconnect"
                connect.graphic = Icon.closeCircle()
            } else {
                connect.text = "Connect"
                connect.graphic = Icon.connection()
            }
        }
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)

        connected.set(connectionService.isConnected())
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null

        connectionService.disconnect()

        camerasPage.close()
    }

    override fun onEvent(event: Any) {
        when (event) {
            is CameraAttached -> attachedCameras.add(event.device)
            is CameraDetached -> attachedCameras.remove(event.device)
        }
    }

    @Synchronized
    private fun connect() {
        if (!connectionService.isConnected()) {
            try {
                val host = host.text.trim().ifEmpty { "localhost" }
                val port = port.text.trim().toIntOrNull() ?: 7624
                connectionService.connect(host, port)
                connected.set(true)
            } catch (e: Throwable) {
                MessageDialog(
                    "A connection to the INDI Server could not be established. Check your connection or server configuration.",
                    "Connection failed"
                ).showAndWait()
            }
        } else {
            connectionService.disconnect()
            connected.set(false)
        }
    }

    @Synchronized
    private fun open(name: String) {
        val page = when (name) {
            "CAMERA" -> camerasPage
            "OPEN_NEW_IMAGE" -> return openNewImage()
            else -> return
        }

        page.show()
    }

    private fun openNewImage() {
        val chooser = FileChooser()
        chooser.title = "Open New Image"
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
        val file = chooser.showOpenDialog(null) ?: return
        val page = imageViewers.firstOrNull { !it.isShowing && it.camera == null } ?: ImageViewer()
        imageViewers.add(page)
        page.open(file)
    }
}
