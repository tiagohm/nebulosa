package nebulosa.desktop.gui.mount

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.mount.MountManager
import nebulosa.desktop.logic.mount.SiteAndTimeManager
import nebulosa.desktop.view.mount.SiteAndTimeView
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class SiteAndTimeWindow(
    override val mount: Mount,
    private val mountManager: MountManager,
) : AbstractWindow("SiteAndTime", "nebulosa-site-and-time"), SiteAndTimeView {

    @FXML private lateinit var latitudeTextField: TextField
    @FXML private lateinit var longitudeTextField: TextField
    @FXML private lateinit var elevationTextField: TextField
    @FXML private lateinit var dateDatePicker: DatePicker
    @FXML private lateinit var timeTextField: TextField
    @FXML private lateinit var offsetTextField: TextField
    @FXML private lateinit var gpsChoiceBox: ChoiceBox<GPS>
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var useCoordinateFromGpsButton: Button
    @FXML private lateinit var syncDateAndTimeButton: Button

    private val siteAndTimeManager = SiteAndTimeManager(this)

    init {
        title = "Site & Time"
        resizable = false
    }

    override fun onCreate() {
        updateSite(mount.longitude, mount.latitude, mount.elevation)
        updateDateAndTime(mount.time)

        dateDatePicker.converter = LocalDateStringConverter

        gpsChoiceBox.converter = GPSStringConverter
        gpsChoiceBox.itemsProperty().bind(mountManager.equipmentManager.attachedGPSs)

        openINDIButton.disableProperty().bind(gpsChoiceBox.selectionModel.selectedItemProperty().isNull)

        useCoordinateFromGpsButton.disableProperty().bind(gpsChoiceBox.selectionModel.selectedItemProperty().isNull)
    }

    override var longitude
        get() = longitudeTextField.text.trim()
            .ifBlank { null }
            ?.let(Angle::from)
            ?: mount.longitude
        set(value) {
            longitudeTextField.text = value.format(AngleFormatter.SIGNED_DMS)
        }

    override var latitude
        get() = latitudeTextField.text.trim()
            .ifBlank { null }
            ?.let(Angle::from)
            ?: mount.latitude
        set(value) {
            latitudeTextField.text = value.format(AngleFormatter.SIGNED_DMS)
        }

    override var elevation
        get() = elevationTextField.text.trim().toDoubleOrNull()?.m ?: mount.elevation
        set(value) {
            elevationTextField.text = value.meters.toString()
        }

    override val gps: GPS?
        get() = gpsChoiceBox.value

    override var date: LocalDate
        get() = dateDatePicker.value
        set(value) {
            dateDatePicker.value = value
        }

    override var time: LocalTime
        get() = LocalTime.parse(timeTextField.text.trim(), TIME_FORMAT)
        set(value) {
            timeTextField.text = value.format(TIME_FORMAT)
        }

    override var offset
        get() = offsetTextField.text.trim().toDoubleOrNull() ?: 0.0
        set(value) {
            offsetTextField.text = "%.1f".format(value)
        }

    @FXML
    private fun openINDI() {
        siteAndTimeManager.openINDIPanelControl()
    }

    @FXML
    private fun useCoordinateFromGps() {
        siteAndTimeManager.useCoordinateFromGPS()
    }

    @FXML
    private fun syncDateAndTime() {
        siteAndTimeManager.syncDateAndTime()
    }

    @FXML
    private fun applySite() {
        siteAndTimeManager.applySite()
    }

    @FXML
    private fun applyDateAndTime() {
        siteAndTimeManager.applyDateAndTime()
    }

    override fun openINDIPanelControl(gps: GPS) {
        mountManager.indiPanelControlWindow.show(bringToFront = true)
        mountManager.indiPanelControlWindow.device = gps
    }

    override fun updateSite(longitude: Angle, latitude: Angle, elevation: Distance) {
        this.longitude = longitude
        this.latitude = latitude
        this.elevation = elevation
    }

    override fun updateDateAndTime(dateTime: OffsetDateTime) {
        date = dateTime.toLocalDate()
        time = dateTime.toLocalTime()
        offset = dateTime.offset.totalSeconds / 60.0
    }

    private object GPSStringConverter : StringConverter<GPS>() {

        override fun toString(device: GPS?) = device?.name ?: "No GPS selected"

        override fun fromString(text: String?) = null
    }

    private object LocalDateStringConverter : StringConverter<LocalDate>() {

        override fun toString(date: LocalDate?) = date?.format(DATE_FORMAT)

        override fun fromString(text: String?) = text?.let { LocalDate.parse(it, DATE_FORMAT) }
    }

    companion object {

        @JvmStatic private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        @JvmStatic private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    }
}
