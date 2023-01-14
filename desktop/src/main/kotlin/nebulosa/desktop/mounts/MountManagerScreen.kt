package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.beans.transformed
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.core.util.concurrent.Ticker
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.telescopecontrol.TelescopeControlServerScreen
import nebulosa.indi.devices.guiders.GuiderEvent
import nebulosa.indi.devices.guiders.GuiderPulsingChanged
import nebulosa.indi.devices.mounts.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.nova.position.Geoid
import nebulosa.time.TimeJD
import nebulosa.time.UTC
import org.controlsfx.control.SegmentedButton
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MountManagerScreen : Screen("MountManager", "nebulosa-mount-manager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var mounts: ChoiceBox<Mount>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var openINDI: Button
    @FXML private lateinit var rightAscension: Label
    @FXML private lateinit var declination: Label
    @FXML private lateinit var rightAscensionJ2000: Label
    @FXML private lateinit var declinationJ2000: Label
    @FXML private lateinit var altitude: Label
    @FXML private lateinit var azimuth: Label
    @FXML private lateinit var pierSide: Label
    @FXML private lateinit var meridianAt: Label
    @FXML private lateinit var lst: Label
    @FXML private lateinit var targetCoordinatesEquinox: SegmentedButton
    @FXML private lateinit var siteAndTime: Button
    @FXML private lateinit var targetRightAscension: TextField
    @FXML private lateinit var targetDeclination: TextField
    @FXML private lateinit var goTo: Button
    @FXML private lateinit var slewTo: Button
    @FXML private lateinit var sync: Button
    @FXML private lateinit var targetCoordinatesMenu: ContextMenu
    @FXML private lateinit var telescopeControlServer: Button
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
    @FXML private lateinit var trackingMode: ChoiceBox<TrackMode>
    @FXML private lateinit var slewSpeed: ChoiceBox<String>
    @FXML private lateinit var park: Button
    @FXML private lateinit var home: Button
    @FXML private lateinit var status: Label

    private val siteAndTimeScreen = SiteAndTimeScreen()

    private val subscribers = arrayOfNulls<Disposable>(2)
    @Volatile private var ticker: Ticker? = null

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
        equipmentManager.selectedMount.isConnected.on { connect.styleClass.toggle("text-red-700", "text-blue-grey-700") }

        openINDI.disableProperty().bind(connect.disableProperty())

        rightAscension.textProperty()
            .bind(equipmentManager.selectedMount.rightAscension.transformed { Angle.formatHMS(it.hours, "%02dh %02dm %05.02fs") })

        declination.textProperty().bind(equipmentManager.selectedMount.declination.transformed { Angle.formatDMS(it.deg, "%s%02d° %02d' %05.02f\"") })

        rightAscensionJ2000.textProperty()
            .bind(equipmentManager.selectedMount.rightAscensionJ2000.transformed { Angle.formatHMS(it.hours, "%02dh %02dm %05.02fs") })

        declinationJ2000.textProperty()
            .bind(equipmentManager.selectedMount.declinationJ2000.transformed { Angle.formatDMS(it.deg, "%s%02d° %02d' %05.02f\"") })

        pierSide.textProperty().bind(equipmentManager.selectedMount.pierSide.asString())

        targetCoordinatesEquinox.disableProperty().bind(isNotConnectedOrSlewing)

        siteAndTime.disableProperty().bind(isNotConnectedOrSlewing)

        targetRightAscension.disableProperty().bind(isNotConnectedOrSlewing)

        targetDeclination.disableProperty().bind(isNotConnectedOrSlewing)

        goTo.disableProperty().bind(isNotConnectedOrSlewing)
        slewTo.disableProperty().bind(isNotConnectedOrSlewing)
        sync.disableProperty().bind(isNotConnectedOrSlewing or !equipmentManager.selectedMount.canSync)

        targetCoordinatesMenu.items
            .filter { it.userData == "BIND_TO_SELECTED_MOUNT" }
            .forEach { it.disableProperty().bind(isNotConnectedOrSlewing) }

        telescopeControlServer.disableProperty().bind(isNotConnectedOrSlewing)

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
        trackingMode.itemsProperty().bind(equipmentManager.selectedMount.trackModes)
        equipmentManager.selectedMount.trackMode.on { trackingMode.value = it }
        trackingMode.valueProperty().on { if (it != null) equipmentManager.selectedMount.get().trackingMode(it) }

        slewSpeed.disableProperty().bind(isNotConnectedOrSlewing)
        slewSpeed.itemsProperty().bind(equipmentManager.selectedMount.slewRates)
        equipmentManager.selectedMount.slewRate.on { slewSpeed.value = it }
        slewSpeed.valueProperty().on { if (it != null) equipmentManager.selectedMount.get().slewRate(it) }

        park.disableProperty().bind(isNotConnectedOrSlewing or !equipmentManager.selectedMount.canPark)
        park.textProperty().bind(equipmentManager.selectedMount.isParked.between("Unpark", "Park"))
        (park.graphic as Label).textProperty().bind(equipmentManager.selectedMount.isParked.between(MaterialIcon.PLAY, MaterialIcon.STOP))
        equipmentManager.selectedMount.isParked.on { (park.graphic as Label).styleClass.toggle("text-red-700", "text-blue-grey-700") }

        home.disableProperty().set(true)

        equipmentManager.selectedMount.on {
            title = "Mount · ${it?.name}"
            updateStatus()
        }

        preferences.double("mountManager.screen.x")?.let { x = it }
        preferences.double("mountManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("mountManager.screen.x", it) }
        yProperty().on { preferences.double("mountManager.screen.y", it) }
    }

    override fun onStart() {
        subscribers[0] = eventBus
            .filterIsInstance<MountEvent> { it.device === equipmentManager.selectedMount.get() }
            .subscribe(::onMountEvent)

        subscribers[1] = eventBus
            .filterIsInstance<GuiderEvent<*>> { it.device === equipmentManager.selectedMount.get() }
            .subscribe(::onGuiderEvent)

        val mount = equipmentManager.selectedMount.get()

        if (mount !in equipmentManager.attachedMounts) {
            mounts.selectionModel.select(null)
        }

        ticker?.interrupt()
        ticker = Ticker(::onTick, 1000L)
        ticker!!.start()
    }

    override fun onStop() {
        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)

        ticker?.interrupt()
        ticker = null
    }

    private fun onMountEvent(event: MountEvent) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged -> Platform.runLater { updateStatus() }
        }
    }

    private fun onGuiderEvent(event: GuiderEvent<*>) {
        when (event) {
            is GuiderPulsingChanged -> Platform.runLater { updateStatus() }
        }
    }

    val localSiderealTime: Angle
        get() {
            val mount = equipmentManager.selectedMount.get() ?: return Angle.ZERO
            val position = Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)
            return position.lstAt(UTC(TimeJD.now()))
        }

    private fun onTick() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val computedLST = localSiderealTime
        val timeLeftToMeridianFlip = (mount.rightAscension - computedLST).normalized
        val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

        Platform.runLater {
            meridianAt.text =
                "%s (%s)".format(timeToMeridianFlip.format(MERIDIAN_TIME_FORMAT), Angle.formatHMS(timeLeftToMeridianFlip, "-%02d:%02d:%02.0f"))
            lst.text = Angle.formatHMS(computedLST, "%02d:%02d:%02.0f ")
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
    private fun openINDI() {
        val mount = equipmentManager.selectedMount.get() ?: return
        screenManager.openINDIPanelControl(mount)
    }

    @FXML
    private fun openTargetCoordinatesMenu(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            targetCoordinatesMenu.show(event.source as Node, event.screenX, event.screenY)
            event.consume()
        }
    }

    @FXML
    private fun openTelescopeControlServer() {
        val mount = equipmentManager.selectedMount.get() ?: return
        val screen = TelescopeControlServerScreen(mount)
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

    @FXML
    private fun openSiteAndTime() {
        siteAndTimeScreen.load(equipmentManager.selectedMount.get() ?: return)
    }

    val targetCoordinates: Pair<Angle, Angle>
        get() {
            val ra = Angle.parseCoordinatesAsDouble(targetRightAscension.text)
            val dec = Angle.parseCoordinatesAsDouble(targetDeclination.text)
            require(ra in 0.0..24.0)
            require(dec in -90.0..90.0)
            return ra.hours to dec.deg
        }

    @FXML
    private fun goTo() {
        try {
            val mount = equipmentManager.selectedMount.get() ?: return
            val (ra, dec) = targetCoordinates
            val isJ2000 = targetCoordinatesEquinox.toggleGroup.selectedToggle.userData == "J2000"
            if (isJ2000) mount.goToJ2000(ra, dec)
            else mount.goTo(ra, dec)
        } catch (e: Throwable) {
            showAlert("Invalid target coordinates")
        }
    }

    @FXML
    private fun slewTo() {
        try {
            val mount = equipmentManager.selectedMount.get() ?: return
            val (ra, dec) = targetCoordinates
            val isJ2000 = targetCoordinatesEquinox.toggleGroup.selectedToggle.userData == "J2000"
            if (isJ2000) mount.slewToJ2000(ra, dec)
            else mount.slewTo(ra, dec)
        } catch (e: Throwable) {
            showAlert("Invalid target coordinates")
        }
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
    private fun loadCurrentPosition() {
        targetRightAscension.text = rightAscension.text
        targetDeclination.text = declination.text
        targetCoordinatesEquinox.toggleGroup.selectToggle(targetCoordinatesEquinox.buttons[0])
    }

    @FXML
    private fun loadCurrentPositionJ2000() {
        targetRightAscension.text = rightAscensionJ2000.text
        targetDeclination.text = declinationJ2000.text
        targetCoordinatesEquinox.toggleGroup.selectToggle(targetCoordinatesEquinox.buttons[1])
    }

    // TODO: Prevent go to below horizon. Show warning.

    @FXML
    private fun loadZenithPosition() {
        val mount = equipmentManager.selectedMount.get() ?: return
        targetRightAscension.text = Angle.formatHMS(localSiderealTime, "%02dh %02dm %05.02fs")
        targetDeclination.text = Angle.formatDMS(mount.latitude, "%s%02d° %02d' %05.02f\"")
        targetCoordinatesEquinox.toggleGroup.selectToggle(targetCoordinatesEquinox.buttons[0])
    }

    @FXML
    private fun loadNorthPolePosition() {
        targetRightAscension.text = Angle.formatHMS(localSiderealTime, "%02dh %02dm %05.02fs")
        targetDeclination.text = "+90° 00' 00\""
        targetCoordinatesEquinox.toggleGroup.selectToggle(targetCoordinatesEquinox.buttons[0])
    }

    @FXML
    private fun loadSouthPolePosition() {
        targetRightAscension.text = Angle.formatHMS(localSiderealTime, "%02dh %02dm %05.02fs")
        targetDeclination.text = "-90° 00' 00\""
        targetCoordinatesEquinox.toggleGroup.selectToggle(targetCoordinatesEquinox.buttons[0])
    }

    @FXML
    private fun abort() {
        val mount = equipmentManager.selectedMount.get() ?: return
        mount.abortMotion()
    }

    @FXML
    private fun home() {
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
        else if (mount.isPulseGuiding) "guiding"
        else "idle"
    }

    companion object {

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
