package nebulosa.desktop.gui.mount

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.StringConverter
import nebulosa.desktop.gui.*
import nebulosa.desktop.logic.*
import nebulosa.desktop.logic.mount.MountManager
import nebulosa.desktop.logic.util.toggle
import nebulosa.desktop.view.mount.MountView
import nebulosa.erfa.PairOfAngle
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.AngleFormatter
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

    private val mountManager = MountManager(this)

    init {
        title = "Mount"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = mountManager.connectedProperty.not()
        val isConnecting = mountManager.connectingProperty
        val isMoving = mountManager.slewingProperty or mountManager.parkingProperty
        val isNotConnectedOrMoving = isNotConnected or isMoving

        mountChoiceBox.converter = MountStringConverter
        mountChoiceBox.disableProperty().bind(isConnecting or isMoving)
        mountChoiceBox.itemsProperty().bind(mountManager.mounts)
        mountManager.bind(mountChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(mountManager.isNull() or isConnecting or isMoving)
        connectButton.textProperty().bind(mountManager.connectedProperty.between(CLOSE_CIRCLE_ICON, CONNECTION_ICON))
        mountManager.connectedProperty.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700", it) }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        rightAscensionLabel.textProperty()
            .bind(mountManager.rightAscensionProperty.asString { it.hours.format(AngleFormatter.HMS) })

        declinationLabel.textProperty().bind(mountManager.declinationProperty.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        rightAscensionJ2000Label.textProperty().bind(mountManager.rightAscensionJ2000Property.asString { it.hours.format(AngleFormatter.HMS) })

        declinationJ2000Label.textProperty().bind(mountManager.declinationJ2000Property.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        azimuthLabel.textProperty().bind(mountManager.azimuthProperty.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        altitudeLabel.textProperty().bind(mountManager.altitudeProperty.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        pierSideLabel.textProperty().bind(mountManager.pierSideProperty.asString())

        targetCoordinatesEquinoxSegmentedButton.disableProperty().bind(isNotConnectedOrMoving)

        siteAndTimeButton.disableProperty().bind(isNotConnectedOrMoving)

        targetRightAscensionTextField.disableProperty().bind(isNotConnectedOrMoving)

        targetDeclinationTextField.disableProperty().bind(isNotConnectedOrMoving)

        goToButton.disableProperty().bind(isNotConnectedOrMoving)
        slewToButton.disableProperty().bind(isNotConnectedOrMoving)
        syncButton.disableProperty().bind(isNotConnectedOrMoving or !mountManager.canSyncProperty)

        targetCoordinatesContextMenu.items
            .filter { it.userData == "BIND_TO_SELECTED_MOUNT" }
            .forEach { it.disableProperty().bind(isNotConnectedOrMoving) }

        telescopeControlServerButton.disableProperty().bind(isNotConnectedOrMoving)

        nudgeNEButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeNButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeNWButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeEButton.disableProperty().bind(isNotConnectedOrMoving)
        abortButton.disableProperty().bind(isNotConnected or !mountManager.canAbortProperty)
        nudgeWButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeSEButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeSButton.disableProperty().bind(isNotConnectedOrMoving)
        nudgeSWButton.disableProperty().bind(isNotConnectedOrMoving)

        trackingToggleSwitch.disableProperty().bind(isNotConnectedOrMoving)
        mountManager.trackingProperty.on(trackingToggleSwitch::setSelected)
        trackingToggleSwitch.selectedProperty().on { mountManager.get().tracking(it) }

        trackingModeChoiceBox.disableProperty().bind(isNotConnectedOrMoving)
        trackingModeChoiceBox.itemsProperty().bind(mountManager.trackModesProperty)
        mountManager.trackModeProperty.on { trackingModeChoiceBox.value = it }
        trackingModeChoiceBox.valueProperty().on { if (it != null) mountManager.get().trackingMode(it) }

        slewSpeedChoiceBox.disableProperty().bind(isNotConnectedOrMoving)
        slewSpeedChoiceBox.itemsProperty().bind(mountManager.slewRatesProperty)
        mountManager.slewRateProperty.on { slewSpeedChoiceBox.value = it }
        slewSpeedChoiceBox.valueProperty().on { if (it != null) mountManager.get().slewRate(it) }

        parkButton.disableProperty().bind(isNotConnectedOrMoving or !mountManager.canParkProperty)
        parkButton.textProperty().bind(mountManager.parkedProperty.between("Unpark", "Park"))
        val parkIcon = parkButton.graphic as Label
        parkIcon.textProperty().bind(mountManager.parkedProperty.between(PLAY_ICON, STOP_ICON))
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

    override val targetCoordinates: PairOfAngle
        get() {
            val ra = Angle.from(targetRightAscensionTextField.text, true)!!
            val dec = Angle.from(targetDeclinationTextField.text)!!
            return PairOfAngle(ra.normalized, dec)
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
        mountManager.openINDIPanelControl()
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
        val direction = (event.source as Node).userData as String
    }

    @FXML
    private fun openSiteAndTime() {
        mountManager.openSiteAndTime()
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
        targetRightAscensionTextField.text = ra.format(AngleFormatter.HMS)
        targetDeclinationTextField.text = dec.format(AngleFormatter.SIGNED_DMS)
    }

    override fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime) {
        meridianAtLabel.text = "%s (%s)".format(timeToMeridianFlip.format(MERIDIAN_TIME_FORMAT), timeLeftToMeridianFlip.format(LST_FORMAT))
        lstLabel.text = lst.format(LST_FORMAT)
    }

    private object MountStringConverter : StringConverter<Mount>() {

        override fun toString(device: Mount?) = device?.name ?: "No mount selected"

        override fun fromString(text: String?) = null
    }

    companion object {

        @JvmStatic private val LST_FORMAT = AngleFormatter.Builder()
            .hours()
            .noSign()
            .secondsDecimalPlaces(0)
            .build()

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")

        @JvmStatic private var window: MountWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = MountWindow()
            window!!.show(bringToFront = true)
        }
    }
}
