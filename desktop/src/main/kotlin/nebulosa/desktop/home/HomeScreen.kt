package nebulosa.desktop.home

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import nebulosa.desktop.cameras.CameraManagerScreen
import nebulosa.desktop.connections.ConnectionManager
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.filterwheels.FilterWheelManagerScreen
import nebulosa.desktop.focusers.FocuserManagerScreen
import nebulosa.desktop.imageviewer.ImageViewerScreen
import nebulosa.desktop.mounts.MountManagerScreen
import nebulosa.desktop.platesolving.PlateSolverScreen
import org.koin.core.component.get
import org.koin.core.component.inject

class HomeScreen : Screen("Home") {

    private val connectionManager by inject<ConnectionManager>()
    private val equipmentManager by inject<EquipmentManager>()

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

    private val screens = HashSet<Screen>()
    private val imageViewers = HashSet<ImageViewerScreen>(8)

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Nebulosa"
        isResizable = false
    }

    override fun onCreate() {
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

        host.disableProperty().bind(equipmentManager.connected)
        port.disableProperty().bind(equipmentManager.connected)
        cameras.disableProperty().bind(equipmentManager.connected.not())
        mounts.disableProperty().bind(equipmentManager.connected.not())
        guiders.disableProperty().bind(equipmentManager.connected.not())
        filterWheels.disableProperty().bind(equipmentManager.connected.not())
        focusers.disableProperty().bind(equipmentManager.connected.not())
        domes.disableProperty().bind(equipmentManager.connected.not())
        rotators.disableProperty().bind(equipmentManager.connected.not())
        switches.disableProperty().bind(equipmentManager.connected.not())
        alignment.disableProperty().bind(equipmentManager.connected.not())
        sequencer.disableProperty().bind(equipmentManager.connected.not())

        equipmentManager.connected.addListener { _, _, value ->
            connect.graphic = if (value) Icon.closeCircle() else Icon.connection()
        }

        host.text = preferences.string("connection.last.host") ?: ""
        port.text = preferences.string("connection.last.port") ?: ""

        preferences.double("home.screen.x")?.let { x = it }
        preferences.double("home.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("home.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("home.screen.y", value.toDouble()) }
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null

        connectionManager.disconnect()

        imageViewers.forEach(Screen::close)
        imageViewers.clear()

        screens.forEach(Screen::close)
        screens.clear()
    }

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

    @Synchronized
    private fun open(name: String) {
        val page = when (name) {
            "CAMERA" -> get<CameraManagerScreen>()
            "MOUNT" -> get<MountManagerScreen>()
            "FOCUSER" -> get<FocuserManagerScreen>()
            "FILTER_WHEEL" -> get<FilterWheelManagerScreen>()
            "PLATE_SOLVING" -> PlateSolverScreen()
            "OPEN_NEW_IMAGE" -> return openNewImage()
            else -> return
        }

        screens.add(page)

        page.show()
    }

    private fun openNewImage() {
        val chooser = FileChooser()
        chooser.title = "Open New Image"
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
        val file = chooser.showOpenDialog(this) ?: return
        val page = imageViewers.firstOrNull { !it.isShowing && it.camera == null } ?: ImageViewerScreen()
        imageViewers.add(page)
        page.open(file)
    }
}
