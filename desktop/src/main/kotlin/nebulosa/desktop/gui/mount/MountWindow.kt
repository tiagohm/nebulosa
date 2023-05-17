package nebulosa.desktop.gui.mount

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.CopyableLabel
import nebulosa.desktop.gui.control.SwitchSegmentedButton
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.*
import nebulosa.desktop.logic.mount.MountManager
import nebulosa.desktop.view.mount.MountView
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.SlewRate
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.AngleFormatter
import nebulosa.nova.astrometry.Constellation
import org.controlsfx.control.SegmentedButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class MountWindow : AbstractWindow("Mount", "telescope"), MountView {

    @Lazy @Autowired private lateinit var mountManager: MountManager

    @FXML private lateinit var mountChoiceBox: ChoiceBox<Mount>
    @FXML private lateinit var connectButton: TwoStateButton
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var rightAscensionLabel: CopyableLabel
    @FXML private lateinit var declinationLabel: CopyableLabel
    @FXML private lateinit var rightAscensionJ2000Label: CopyableLabel
    @FXML private lateinit var declinationJ2000Label: CopyableLabel
    @FXML private lateinit var altitudeLabel: CopyableLabel
    @FXML private lateinit var azimuthLabel: CopyableLabel
    @FXML private lateinit var pierSideLabel: CopyableLabel
    @FXML private lateinit var meridianAtLabel: CopyableLabel
    @FXML private lateinit var lstLabel: CopyableLabel
    @FXML private lateinit var constellationLabel: CopyableLabel
    @FXML private lateinit var targetCoordinatesEquinoxSegmentedButton: SegmentedButton
    @FXML private lateinit var siteAndTimeButton: Button
    @FXML private lateinit var targetRightAscensionTextField: TextField
    @FXML private lateinit var targetDeclinationTextField: TextField
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var targetCoordinatesContextMenuButton: Button
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
    @FXML private lateinit var trackingSwitch: SwitchSegmentedButton
    @FXML private lateinit var trackingModeChoiceBox: ChoiceBox<TrackMode>
    @FXML private lateinit var slewSpeedChoiceBox: ChoiceBox<SlewRate>
    @FXML private lateinit var parkButton: TwoStateButton
    @FXML private lateinit var homeButton: Button
    @FXML private lateinit var statusLabel: CopyableLabel
    @FXML private lateinit var targetAzimuthLabel: CopyableLabel
    @FXML private lateinit var targetAltitudeLabel: CopyableLabel
    @FXML private lateinit var targetConstellationLabel: CopyableLabel
    @FXML private lateinit var targetMeridianAtLabel: CopyableLabel

    private val nudgeButtonPressed = SimpleObjectProperty<Node>()

    init {
        title = "Mount"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = mountManager.connectedProperty.not()
        val isConnecting = mountManager.connectingProperty
        val isMoving = mountManager.slewingProperty or mountManager.parkingProperty
        val isNotConnectedOrMoving = isNotConnected or isMoving

        mountManager.initialize()

        mountChoiceBox.converter = MountStringConverter
        mountChoiceBox.disableProperty().bind(isConnecting or isMoving)
        mountChoiceBox.itemsProperty().bind(mountManager.mounts)
        mountManager.bind(mountChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(mountManager.isNull() or isConnecting or isMoving)
        mountManager.connectedProperty.on { connectButton.state = it }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        rightAscensionLabel.textProperty()
            .bind(mountManager.rightAscensionProperty.asString { it.hours.format(AngleFormatter.HMS) })

        declinationLabel.textProperty().bind(mountManager.declinationProperty.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        rightAscensionJ2000Label.textProperty().bind(mountManager.rightAscensionJ2000Property.asString { it.hours.format(AngleFormatter.HMS) })

        declinationJ2000Label.textProperty().bind(mountManager.declinationJ2000Property.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        azimuthLabel.textProperty().bind(mountManager.azimuthProperty.asString { it.deg.format(AngleFormatter.DMS) })

        altitudeLabel.textProperty().bind(mountManager.altitudeProperty.asString { it.deg.format(AngleFormatter.SIGNED_DMS) })

        pierSideLabel.textProperty().bind(mountManager.pierSideProperty.asString())

        constellationLabel.textProperty().bind(mountManager.constellationProperty.asString { it?.iau ?: "-" })

        targetCoordinatesEquinoxSegmentedButton.disableProperty().bind(isNotConnectedOrMoving)

        siteAndTimeButton.disableProperty().bind(isNotConnectedOrMoving)

        targetRightAscensionTextField.disableProperty().bind(isNotConnectedOrMoving)
        targetRightAscensionTextField.textProperty().on { targetRightAscension = Angle.from(it, true) ?: Angle.NaN }

        targetDeclinationTextField.disableProperty().bind(isNotConnectedOrMoving)
        targetDeclinationTextField.textProperty().on { targetDeclination = Angle.from(it) ?: Angle.NaN }

        goToButton.disableProperty().bind(isNotConnectedOrMoving)
        slewToButton.disableProperty().bind(isNotConnectedOrMoving)
        syncButton.disableProperty().bind(isNotConnectedOrMoving or !mountManager.canSyncProperty)

        targetCoordinatesContextMenuButton.disableProperty().bind(isNotConnectedOrMoving)

        telescopeControlServerButton.disableProperty().bind(isNotConnectedOrMoving)

        nudgeNEButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeNEButton })
        nudgeNButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeNButton })
        nudgeNWButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeNWButton })
        nudgeEButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeEButton })
        abortButton.disableProperty().bind(isNotConnected or !mountManager.canAbortProperty)
        nudgeWButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeWButton })
        nudgeSEButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeSEButton })
        nudgeSButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeSButton })
        nudgeSWButton.disableProperty().bind(isNotConnectedOrMoving and nudgeButtonPressed.asBoolean { it !== nudgeSWButton })

        trackingSwitch.disableProperty().bind(isNotConnectedOrMoving)
        mountManager.trackingProperty.on { trackingSwitch.state = it }
        trackingSwitch.stateProperty.on { mountManager.toggleTracking(it) }

        trackingModeChoiceBox.converter = TrackModeStringConverter
        trackingModeChoiceBox.disableProperty().bind(isNotConnectedOrMoving)
        trackingModeChoiceBox.itemsProperty().bind(mountManager.trackModesProperty)
        mountManager.trackModeProperty.on { trackingModeChoiceBox.value = it }
        trackingModeChoiceBox.valueProperty().on { if (it != null) mountManager.toggleTrackingMode(it) }

        slewSpeedChoiceBox.converter = SlewRateStringConverter
        slewSpeedChoiceBox.disableProperty().bind(isNotConnectedOrMoving)
        slewSpeedChoiceBox.itemsProperty().bind(mountManager.slewRatesProperty)
        mountManager.slewRateProperty.on { slewSpeedChoiceBox.value = it }
        slewSpeedChoiceBox.valueProperty().on { if (it != null) mountManager.toggleSlewRate(it) }

        parkButton.disableProperty().bind(isNotConnectedOrMoving or !mountManager.canParkProperty)
        mountManager.parkedProperty.on { parkButton.state = it }

        homeButton.disableProperty().bind(isNotConnectedOrMoving or !mountManager.canHomeProperty)
    }

    override fun onStart() {
        mountManager.loadPreferences()
    }

    override fun onStop() {
        mountManager.savePreferences()
    }

    override var status
        get() = statusLabel.text ?: ""
        set(value) {
            statusLabel.text = value
        }

    override var targetRightAscension = Angle.ZERO
        protected set

    override var targetDeclination = Angle.ZERO
        protected set

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
    private fun openINDIPanelControl() {
        launch { mountManager.openINDIPanelControl() }
    }

    @FXML
    private fun openTelescopeControlServer() {
        launch { mountManager.openTelescopeControlServer() }
    }

    @FXML
    private fun park() {
        mountManager.park()
    }

    @FXML
    private fun nudgeTo(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY && event.clickCount == 1) {
            val button = event.source as Node
            val direction = button.userData as String
            val enable = event.eventType == MouseEvent.MOUSE_PRESSED
            nudgeButtonPressed.set(if (enable) button else null)
            direction.forEach { mountManager.nudgeTo(it, enable) }
        }
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
    private fun loadLocation(event: ActionEvent) {
        when ((event.source as MenuItem).userData as String) {
            "JNOW" -> launch { mountManager.loadCurrentLocation() }
            "J2000" -> launch { mountManager.loadCurrentLocationJ2000() }
            "ZENITH" -> launch { mountManager.loadZenithLocation() }
            "NORTH_POLE" -> launch { mountManager.loadNorthCelestialPoleLocation() }
            "SOUTH_POLE" -> launch { mountManager.loadSouthCelestialPoleLocation() }
            "GALACTIC_CENTER" -> launch { mountManager.loadGalacticCenterLocation() }
        }
    }

    // TODO: Prevent go to below horizon. Show warning.
    // TODO: Prevent go to Sun. Show warning.

    @FXML
    private fun abort() {
        mountManager.abort()
    }

    @FXML
    private fun home() {
        mountManager.home()
    }

    @FXML
    private fun toggleTrackingMode(event: ActionEvent) {
        val mode = TrackMode.valueOf((event.source as Node).userData as String)
        mountManager.toggleTrackingMode(mode)
    }

    override suspend fun updateTargetPosition(ra: Angle, dec: Angle) = withMain {
        targetRightAscensionTextField.text = ra.format(AngleFormatter.HMS)
        targetDeclinationTextField.text = dec.format(AngleFormatter.SIGNED_DMS)
    }

    override suspend fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime) = withMain {
        meridianAtLabel.text = "%s (%s)".format(timeToMeridianFlip.format(MERIDIAN_TIME_FORMAT), timeLeftToMeridianFlip.format(LST_FORMAT))
        lstLabel.text = lst.format(LST_FORMAT)
    }

    override suspend fun updateTargetInfo(
        azimuth: Angle, altitude: Angle, constellation: Constellation,
        timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime,
    ) = withMain {
        targetAzimuthLabel.text = azimuth.format(AngleFormatter.DMS)
        targetAltitudeLabel.text = altitude.format(AngleFormatter.SIGNED_DMS)
        targetConstellationLabel.text = constellation.iau
        targetMeridianAtLabel.text = "%s (%s)".format(timeToMeridianFlip.format(MERIDIAN_TIME_FORMAT), timeLeftToMeridianFlip.format(LST_FORMAT))
    }

    private object MountStringConverter : StringConverter<Mount>() {

        override fun toString(device: Mount?) = device?.name ?: "No mount selected"

        override fun fromString(text: String?) = null
    }

    private object TrackModeStringConverter : StringConverter<TrackMode>() {

        override fun toString(rate: TrackMode?) = when (rate) {
            TrackMode.SIDEREAL -> "Sidereal"
            TrackMode.LUNAR -> "Lunar"
            TrackMode.SOLAR -> "Solar"
            TrackMode.KING -> "King"
            TrackMode.CUSTOM -> "Custom"
            null -> "No track mode selected"
        }

        override fun fromString(text: String?) = null
    }

    private object SlewRateStringConverter : StringConverter<SlewRate>() {

        override fun toString(rate: SlewRate?) = rate?.label ?: "No slew rate selected"

        override fun fromString(text: String?) = null
    }

    companion object {

        @JvmStatic private val LST_FORMAT = AngleFormatter.Builder()
            .hours()
            .noSign()
            .secondsDecimalPlaces(0)
            .build()

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
