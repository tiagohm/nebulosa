package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import nebulosa.desktop.core.beans.*
import nebulosa.desktop.core.scene.MaterialColor
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.telescopecontrol.StellariumTelescopeControlScreen
import nebulosa.indi.devices.mounts.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Angle.Companion.rad
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.TimeJD
import org.controlsfx.control.SegmentedButton
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject
import java.util.*
import kotlin.math.abs

class MountManagerScreen : Screen("MountManager", "nebulosa-mount-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var mounts: ChoiceBox<Mount>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var rightAscension: Label
    @FXML private lateinit var declination: Label
    @FXML private lateinit var rightAscensionJ2000: Label
    @FXML private lateinit var declinationJ2000: Label
    @FXML private lateinit var altitude: Label
    @FXML private lateinit var azimuth: Label
    @FXML private lateinit var pierSide: Label
    @FXML private lateinit var targetCoordinatesEquinox: SegmentedButton
    @FXML private lateinit var targetRightAscension: TextField
    @FXML private lateinit var targetDeclination: TextField
    @FXML private lateinit var goTo: Button
    @FXML private lateinit var slewTo: Button
    @FXML private lateinit var sync: Button
    @FXML private lateinit var stellariumTelescopeControl: Button
    @FXML private lateinit var nudgeNE: Button
    @FXML private lateinit var nudgeN: Button
    @FXML private lateinit var nudgeNW: Button
    @FXML private lateinit var nudgeE: Button
    @FXML private lateinit var abort: Button
    @FXML private lateinit var nudgeW: Button
    @FXML private lateinit var nudgeSE: Button
    @FXML private lateinit var nudgeS: Button
    @FXML private lateinit var nudgeSW: Button
    @FXML private lateinit var tracking: ToggleSwitch
    @FXML private lateinit var trackingMode: SegmentedButton
    @FXML private lateinit var trackingModeAdditional: SegmentedButton
    @FXML private lateinit var slewSpeed: ChoiceBox<SlewRate>
    @FXML private lateinit var park: Button
    @FXML private lateinit var status: Label

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Mount"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedMount.isConnected.not()
        val isConnecting = equipmentManager.selectedMount.isConnecting
        val isSlewing = equipmentManager.selectedMount.isSlewing
        val isNotConnectedOrSlewing = isNotConnected or isSlewing

        mounts.converter = DeviceStringConverter()
        mounts.disableProperty().bind(isConnecting or isSlewing)
        mounts.itemsProperty().bind(equipmentManager.attachedMounts)
        equipmentManager.selectedMount.bind(mounts.selectionModel.selectedItemProperty())

        connect.disableProperty().bind(equipmentManager.selectedMount.isNull or isConnecting or isSlewing)
        connect.textProperty().bind(equipmentManager.selectedMount.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        connect.textFillProperty().bind(equipmentManager.selectedMount.isConnected.between(MaterialColor.RED_700, MaterialColor.BLUE_GREY_700))

        rightAscension.textProperty().bind(equipmentManager.selectedMount.rightAscension.transformed { Angle.formatHMS(it.hours) })
        declination.textProperty().bind(equipmentManager.selectedMount.declination.transformed { Angle.formatDMS(it.deg) })
        pierSide.textProperty().bind(equipmentManager.selectedMount.pierSide.asString())

        targetCoordinatesEquinox.disableProperty().bind(isNotConnectedOrSlewing)

        targetRightAscension.disableProperty().bind(isNotConnectedOrSlewing)

        targetDeclination.disableProperty().bind(isNotConnectedOrSlewing)

        goTo.disableProperty().bind(isNotConnectedOrSlewing)
        slewTo.disableProperty().bind(isNotConnectedOrSlewing)
        sync.disableProperty().bind(isNotConnectedOrSlewing or !equipmentManager.selectedMount.canSync)

        stellariumTelescopeControl.disableProperty().bind(isNotConnectedOrSlewing)

        nudgeNE.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeN.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeNW.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeE.disableProperty().bind(isNotConnectedOrSlewing)
        abort.disableProperty().bind(isNotConnected or !equipmentManager.selectedMount.canAbort)
        nudgeW.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeSE.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeS.disableProperty().bind(isNotConnectedOrSlewing)
        nudgeSW.disableProperty().bind(isNotConnectedOrSlewing)

        tracking.disableProperty().bind(isNotConnectedOrSlewing)
        equipmentManager.selectedMount.isTracking.on(tracking::setSelected)
        tracking.selectedProperty().on { equipmentManager.selectedMount.get().tracking(it) }

        trackingMode.disableProperty().bind(isNotConnectedOrSlewing)
        trackingMode.buttons.forEach {
            val mode = TrackMode.valueOf(it.userData as String)
            it.disableProperty().bind(equipmentManager.selectedMount.trackModes notContains mode)
        }

        trackingModeAdditional.disableProperty().bind(isNotConnectedOrSlewing)
        trackingModeAdditional.buttons.forEach {
            val mode = TrackMode.valueOf(it.userData as String)
            it.disableProperty().bind(equipmentManager.selectedMount.trackModes notContains mode)
        }

        equipmentManager.selectedMount.trackMode.onOne { mode ->
            trackingMode.buttons.forEach { it.isSelected = it.userData == mode?.name }
            trackingModeAdditional.buttons.forEach { it.isSelected = it.userData == mode?.name }
            stellariumTelescopeControl.text = "Stellarium Telescope Control"
        }

        slewSpeed.disableProperty().bind(isNotConnectedOrSlewing)
        slewSpeed.itemsProperty().bind(equipmentManager.selectedMount.slewRates)
        equipmentManager.selectedMount.slewRate.onOne(slewSpeed::setValue)

        park.disableProperty().bind(isNotConnectedOrSlewing or !equipmentManager.selectedMount.canPark)

        equipmentManager.selectedMount.onOne {
            title = "Mount · ${it?.name}"
            updateStatus()
            updateCoordinatesJ2000()
        }

        preferences.double("mountManager.screen.x")?.let { x = it }
        preferences.double("mountManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("mountManager.screen.x", it) }
        yProperty().on { preferences.double("mountManager.screen.y", it) }
    }

    override fun onStart() {
        subscriber = eventBus
            .filterIsInstance<MountEvent> { it.device === equipmentManager.selectedMount.get() }
            .subscribe(::onMountEvent)

        val mount = equipmentManager.selectedMount.get()

        if (mount !in equipmentManager.attachedMounts) {
            mounts.selectionModel.select(null)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    private fun onMountEvent(event: MountEvent) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged -> Platform.runLater { updateStatus() }
            is MountEquatorialCoordinatesChanged -> Platform.runLater { updateCoordinatesJ2000() }
        }
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedMount.isConnected.get()) {
            equipmentManager.selectedMount.get().connect()
        } else {
            equipmentManager.selectedMount.get().disconnect()
        }
    }

    @FXML
    private fun openStellariumTelescopeControl() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val screen = StellariumTelescopeControlScreen(mount)
        screen.showAndWait()
    }

    @FXML
    private fun park() {
        val mount = equipmentManager.selectedMount.get() ?: return

        if (mount.isParked) {
            mount.unpark()
        } else if (!mount.isParking) {
            mount.park()
        }
    }

    @FXML
    private fun nudgeTo(event: ActionEvent) {
    }

    val targetCoordinates: Pair<Angle, Angle>?
        get() {
            val ra = parseCoordinates(targetRightAscension.text) ?: return null
            val dec = parseCoordinates(targetDeclination.text) ?: return null
            if (ra < 0.0 || ra >= 24.0) return null
            if (dec < -90.0 || dec >= 90.0) return null
            return ra.hours to dec.deg
        }

    @FXML
    private fun goTo() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val (ra, dec) = targetCoordinates ?: return showAlert("Invalid target coordinates")
        val isJ2000 = targetCoordinatesEquinox.toggleGroup.selectedToggle.userData == "J2000"
        if (isJ2000) mount.goToJ2000(ra, dec)
        else mount.goTo(ra, dec)
    }

    @FXML
    private fun slewTo() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val (ra, dec) = targetCoordinates ?: return showAlert("Invalid target coordinates")
        val isJ2000 = targetCoordinatesEquinox.toggleGroup.selectedToggle.userData == "J2000"
        if (isJ2000) mount.slewToJ2000(ra, dec)
        else mount.slewTo(ra, dec)
    }

    @FXML
    private fun sync() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val (ra, dec) = targetCoordinates ?: return showAlert("Invalid target coordinates")
        val isJ2000 = targetCoordinatesEquinox.toggleGroup.selectedToggle.userData == "J2000"
        if (isJ2000) mount.syncJ2000(ra, dec)
        else mount.sync(ra, dec)
    }

    @FXML
    private fun abort() {
    }

    @FXML
    private fun toggleTrackingMode(event: ActionEvent) {
        val mount = equipmentManager.selectedMount.get() ?: return
        val mode = TrackMode.valueOf((event.source as Node).userData as String)
        mount.trackingMode(mode)
    }

    private fun updateStatus() {
        val mount = equipmentManager.selectedMount.get() ?: return

        status.text = if (mount.isParking) "parking"
        else if (mount.isParked) "parked"
        else if (mount.isSlewing) "slewing"
        else if (mount.isTracking) "tracking"
        else "idle"
    }

    // TODO: Use timer (1s) to call this too (when tracking is on).
    private fun updateCoordinatesJ2000() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val ra = mount.rightAscension.hours
        val dec = mount.declination.deg
        val (raJ2000, decJ2000) = ICRF.equatorial(ra, dec, epoch = TimeJD.now()).equatorialJ2000()
        rightAscensionJ2000.text = Angle.formatHMS(raJ2000.rad.normalized)
        declinationJ2000.text = Angle.formatDMS(decJ2000.rad)
    }

    companion object {

        @JvmStatic private val PARSE_COORDINATES_FACTOR = doubleArrayOf(1.0, 60.0, 3600.0)
        @JvmStatic private val PARSE_COORDINATES_NOT_NUMBER_REGEX = Regex("[^\\-\\d.]+")

        @JvmStatic
        internal fun parseCoordinates(input: String): Double? {
            val trimmedInput = input.trim()
            val decimalInput = trimmedInput.toDoubleOrNull()
            if (decimalInput != null) return decimalInput

            val tokenizer = StringTokenizer(trimmedInput, " \t\n\rhms°'\"")
            var res = 0.0
            var idx = 0
            var negative = false

            while (idx < 3 && tokenizer.hasMoreElements()) {
                val token = tokenizer.nextToken().replace(PARSE_COORDINATES_NOT_NUMBER_REGEX, "").trim()

                if (token.isEmpty()) continue

                if (idx == 0 && token == "-") {
                    negative = true
                    continue
                }

                val value = token.toDoubleOrNull() ?: continue

                if (idx == 0 && value < 0.0) {
                    negative = true
                }

                res += abs(value) / PARSE_COORDINATES_FACTOR[idx++]
            }

            return if (idx == 0) null
            else if (negative) -res
            else res
        }
    }
}
