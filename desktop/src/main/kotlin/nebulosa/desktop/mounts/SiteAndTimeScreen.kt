package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.gps.GPS
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountCoordinateChanged
import nebulosa.indi.devices.mounts.MountEvent
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.m
import org.koin.core.component.inject

class SiteAndTimeScreen : Screen("SiteAndTime", "nebulosa-site-and-time") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var longitude: TextField
    @FXML private lateinit var newLongitude: TextField
    @FXML private lateinit var latitude: TextField
    @FXML private lateinit var newLatitude: TextField
    @FXML private lateinit var elevation: TextField
    @FXML private lateinit var newElevation: TextField
    @FXML private lateinit var gps: ChoiceBox<GPS>
    @FXML private lateinit var coordinateFromGps: Button

    @Volatile private lateinit var mount: Mount
    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Site & Time"
        isResizable = false
    }

    override fun onCreate() {
        gps.converter = DeviceStringConverter()
        gps.itemsProperty().bind(equipmentManager.attachedGPSs)

        coordinateFromGps.disableProperty().bind(gps.selectionModel.selectedItemProperty().isNull)
    }

    override fun onStop() {
        subscriber?.dispose()
    }

    private fun onMountEvent(event: MountEvent) {
        when (event) {
            is MountCoordinateChanged -> Platform.runLater { updateSiteAndTime() }
        }
    }

    fun load(mount: Mount) {
        this.mount = mount

        title = "Site & Time · ${mount.name}"

        subscriber?.dispose()

        subscriber = eventBus
            .filterIsInstance<MountEvent> { it.device === mount }
            .subscribe(::onMountEvent)

        show(bringToFront = true)

        updateSiteAndTime()
    }

    @FXML
    private fun apply() {
        try {
            val longitude = newLongitude.text.trim()
                .ifBlank { null }?.let(Angle::parseCoordinatesAsDouble)?.deg ?: mount.longitude
            val latitude = newLatitude.text.trim()
                .ifBlank { null }?.let(Angle::parseCoordinatesAsDouble)?.deg ?: mount.latitude
            val elevation = newElevation.text.trim().toDoubleOrNull()?.m ?: mount.elevation

            mount.coordinates(longitude, latitude, elevation)
        } catch (e: IllegalArgumentException) {
            return showAlert("Invalid coordinates")
        }
    }

    @FXML
    private fun getCoordinateFromGps() {
        val gps = gps.value ?: return

        newLongitude.text = "${gps.longitude.degrees}"
        newLatitude.text = "${gps.latitude.degrees}"
        newElevation.text = "${gps.elevation.meters}"
    }

    private fun updateSiteAndTime() {
        longitude.text = "${mount.longitude.degrees}"
        latitude.text = "${mount.latitude.degrees}"
        elevation.text = "${mount.elevation.meters}"
    }
}
