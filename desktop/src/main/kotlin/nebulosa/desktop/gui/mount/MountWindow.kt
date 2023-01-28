package nebulosa.desktop.gui.mount

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.StringConverter
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.beans.transformed
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.isNull
import nebulosa.desktop.logic.mount.MountManager
import nebulosa.desktop.mounts.SiteAndTimeScreen
import nebulosa.desktop.view.mount.MountView
import nebulosa.indi.device.mounts.Mount
import nebulosa.indi.device.mounts.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import org.controlsfx.control.SegmentedButton
import org.controlsfx.control.ToggleSwitch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MountWindow : AbstractWindow(), MountView {

    override val resourceName = "Mount"

    override val icon = "nebulosa-mount"

    @FXML private lateinit var mountChoiceBox: ChoiceBox<Mount>
    @FXML private lateinit var connectButton: Button
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var rightAscensionLabel: Label
    @FXML private lateinit var declinationLabel: Label
    @FXML private lateinit var rightAscensionJ2000Label: Label
    @FXML private lateinit var declinationJ2000Label: Label
    @FXML private lateinit var altitudeLabel: Label
    @FXML private lateinit var azimuthLabel: Label
    @FXML private lateinit var pierSideLabel: Label
    @FXML private lateinit var meridianAtLabel: Label
    @FXML private lateinit var lstLabel: Label
    @FXML private lateinit var targetCoordinatesEquinoxSegmentedButton: SegmentedButton
    @FXML private lateinit var siteAndTimeButton: Button
    @FXML private lateinit var targetRightAscensionTextField: TextField
    @FXML private lateinit var targetDeclinationTextField: TextField
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var targetCoordinatesContextMenu: ContextMenu
    @FXML private lateinit var telescopeControlServerButton: Button
    @FXML private lateinit var nudgeNEButton: Button
    @FXML private lateinit var nudgeNButton: Button
    @FXML private lateinit var nudgeNWButton: Button
    @FXML private lateinit var nudgeEButton: Button
    @FXML private lateinit var abortButton: Button
    @FXML private lateinit var nudgeWButton: Button
    @FXML private lateinit var nudgeSEButton: Button
    @FXML private lateinit var nudgeSButton: Button
    @FXML private lateinit var nudgeSWButton: Button
    @FXML private lateinit var trackingToggleSwitch: ToggleSwitch
    @FXML private lateinit var trackingModeChoiceBox: ChoiceBox<TrackMode>
    @FXML private lateinit var slewSpeedChoiceBox: ChoiceBox<String>
    @FXML private lateinit var parkButton: Button
    @FXML private lateinit var homeButton: Button
    @FXML private lateinit var statusLabel: Label

    private val siteAndTimeScreen = SiteAndTimeScreen()

    private val mountManager = MountManager(this)

    init {
        title = "Mount"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = mountManager.connectedProperty.not()
        val isConnecting = mountManager.connectingProperty
        val isSlewing = mountManager.slewingProperty
        val isNotConnectedOrSlewing = isNotConnected or isSlewing

        mountChoiceBox.converter = MountStringConverter
        mountChoiceBox.disableProperty().bind(isConnecting or isSlewing)
        mountChoiceBox.itemsProperty().bind(mountManager.mounts)
        mountManager.bind(mountChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(mountManager.isNull or isConnecting or isSlewing)
        connectButton.textProperty().bind(mountManager.connectedProperty.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        mountManager.connectedProperty.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700", it) }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        rightAscensionLabel.textProperty()
            .bind(mountManager.rightAscensionProperty.transformed { Angle.formatHMS(it.hours, "%02dh %02dm %05.02fs") })

        declinationLabel.textProperty().bind(mountManager.declinationProperty.transformed { Angle.formatDMS(it.deg, "%s%02d° %02d' %05.02f\"") })

        rightAscensionJ2000Label.textProperty()
            .bind(mountManager.rightAscensionJ2000Property.transformed { Angle.formatHMS(it.hours, "%02dh %02dm %05.02fs") })

        declinationJ2000Label.textProperty()
            .bind(mountManager.declinationJ2000Property.transformed { Angle.formatDMS(it.deg, "%s%02d° %02d' %05.02f\"") })

        pierSideLabel.textProperty().bind(mountManager.pierSideProperty.asString())

        targetCoordinatesEquinoxSegmentedButton.disableProperty().bind(isNotConnectedOrSlewing)

        siteAndTimeButton.disableProperty().bind(isNotConnectedOrSlewing)

        targetRightAscensionTextField.disableProperty().bind(isNotConnectedOrSlewing)

        targetDeclinationTextField.disableProperty().bind(isNotConnectedOrSlewing)

        goToButton.disableProperty().bind(isNotConnectedOrSlewing)
        slewToButton.disableProperty().bind(isNotConnectedOrSlewing)
        syncButton.disableProperty().bind(isNotConnectedOrSlewing or !mountManager.canSyncProperty)

        targetCoordinatesContextMenu.items
            .filter { it.userData == "BIND_TO_SELECTED_MOUNT" }
            .forEach { it.disableProperty().bind(isNotConnectedOrSlewing) }

        telescopeControlServerButton.disableProperty().bind(isNotConnectedOrSlewing)

        nudgeNEButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeNButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeNWButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeEButton.disableProperty().bind(isNotConnectedOrSlewing)
        abortButton.disableProperty().bind(isNotConnected or !mountManager.canAbortProperty)
        nudgeWButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeSEButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeSButton.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeSWButton.disableProperty().bind(isNotConnectedOrSlewing)

        trackingToggleSwitch.disableProperty().bind(isNotConnectedOrSlewing)
        mountManager.trackingProperty.on(trackingToggleSwitch::setSelected)
        trackingToggleSwitch.selectedProperty().on { mountManager.get().tracking(it) }

        trackingModeChoiceBox.disableProperty().bind(isNotConnectedOrSlewing)
        trackingModeChoiceBox.itemsProperty().bind(mountManager.trackModesProperty)
        mountManager.trackModeProperty.on { trackingModeChoiceBox.value = it }
        trackingModeChoiceBox.valueProperty().on { if (it != null) mountManager.get().trackingMode(it) }

        slewSpeedChoiceBox.disableProperty().bind(isNotConnectedOrSlewing)
        slewSpeedChoiceBox.itemsProperty().bind(mountManager.slewRatesProperty)
        mountManager.slewRateProperty.on { slewSpeedChoiceBox.value = it }
        slewSpeedChoiceBox.valueProperty().on { if (it != null) mountManager.get().slewRate(it) }

        parkButton.disableProperty().bind(isNotConnectedOrSlewing or !mountManager.canParkProperty)
        parkButton.textProperty().bind(mountManager.parkedProperty.between("Unpark", "Park"))
        val parkIcon = parkButton.graphic as Label
        parkIcon.textProperty().bind(mountManager.parkedProperty.between(MaterialIcon.PLAY, MaterialIcon.STOP))
        mountManager.parkedProperty.on { parkIcon.styleClass.toggle("text-red-700", "text-blue-grey-700", it) }

        homeButton.disableProperty().set(true)
    }

    override fun onStart() {
        mountManager.loadPreferences()
    }

    override fun onStop() {
        mountManager.savePreferences()
    }

    override fun onClose() {
        mountManager.close()
    }

    override var status
        get() = statusLabel.text!!
        set(value) {
            statusLabel.text = value
        }

    override val targetCoordinates: Pair<Angle, Angle>
        get() {
            val ra = Angle.parseCoordinatesAsDouble(targetRightAscensionTextField.text)
            val dec = Angle.parseCoordinatesAsDouble(targetDeclinationTextField.text)
            return ra.hours.normalized to dec.deg.normalized
        }

    override var isJ2000
        get() = targetCoordinatesEquinoxSegmentedButton.toggleGroup.selectedToggle.userData == "J2000"
        set(value) {
            targetCoordinatesEquinoxSegmentedButton.toggleGroup.toggles
                .forEach { it.isSelected = (it.userData == "J2000") == value }
        }

    @FXML
    @Synchronized
    private fun connect() {
        mountManager.connect()
    }

    @FXML
    private fun openINDI() {
    }

    @FXML
    private fun openTargetCoordinatesMenu(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            targetCoordinatesContextMenu.show(event.source as Node, event.screenX, event.screenY)
            event.consume()
        }
    }

    @FXML
    private fun openTelescopeControlServer() {
        mountManager.openTelescopeControlServer()
    }

    @FXML
    private fun park() {
        mountManager.park()
    }

    @FXML
    private fun nudgeTo(event: ActionEvent) {
    }

    @FXML
    private fun openSiteAndTime() {
        // siteAndTimeScreen.load(equipmentManager.selectedMount.get() ?: return)
    }

    @FXML
    private fun goTo() {
        mountManager.goTo()
    }

    @FXML
    private fun slewTo() {
        mountManager.slewTo()
    }

    @FXML
    private fun sync() {
        mountManager.sync()
    }

    @FXML
    private fun loadCurrentPosition() {
        mountManager.loadCurrentPostion()
    }

    @FXML
    private fun loadCurrentPositionJ2000() {
        mountManager.loadCurrentPostionJ2000()
    }

    // TODO: Prevent go to below horizon. Show warning.
    // TODO: Prevent go to Sun. Show warning.

    @FXML
    private fun loadZenithPosition() {
        mountManager.loadZenithPosition()
    }

    @FXML
    private fun loadNorthCelestialPolePosition() {
        mountManager.loadNorthCelestialPolePosition()
    }

    @FXML
    private fun loadSouthCelestialPolePosition() {
        mountManager.loadSouthCelestialPolePosition()
    }

    @FXML
    private fun abort() {
        mountManager.abort()
    }

    @FXML
    private fun home() {
    }

    @FXML
    private fun toggleTrackingMode(event: ActionEvent) {
        val mode = TrackMode.valueOf((event.source as Node).userData as String)
        mountManager.toggleTrackingMode(mode)
    }

    override fun updateTargetPosition(ra: Angle, dec: Angle) {
        targetRightAscensionTextField.text = Angle.formatHMS(ra, RA_FORMAT)
        targetDeclinationTextField.text = Angle.formatDMS(dec, DEC_FORMAT)
    }

    override fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime) {
        meridianAtLabel.text = "%s (%s)".format(timeToMeridianFlip.format(MERIDIAN_TIME_FORMAT), Angle.formatHMS(timeLeftToMeridianFlip, LST_FORMAT))
        lstLabel.text = Angle.formatHMS(lst, "%02d:%02d:%02.0f ")
    }

    private object MountStringConverter : StringConverter<Mount>() {

        override fun toString(device: Mount?) = device?.name ?: "No mount selected"

        override fun fromString(text: String?) = null
    }

    companion object {

        private const val RA_FORMAT = "%02dh %02dm %05.02fs"
        private const val DEC_FORMAT = "%s%02d° %02d' %05.02f\""
        private const val LST_FORMAT = "-%02d:%02d:%02.0f"

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")

        @JvmStatic private var window: MountWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = MountWindow()
            window!!.show(bringToFront = true)
        }
    }
}
