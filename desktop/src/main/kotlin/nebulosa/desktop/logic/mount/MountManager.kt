package nebulosa.desktop.logic.mount

import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.desktop.gui.mount.SiteAndTimeWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.desktop.view.mount.MountView
import nebulosa.desktop.view.telescopecontrol.TelescopeControlView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Distance
import nebulosa.nova.position.Geoid
import nebulosa.time.InstantOfTime
import nebulosa.time.UTC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
class MountManager(
    @Autowired private val view: MountView,
    @Autowired private val equipmentManager: EquipmentManager,
) : MountProperty by equipmentManager.selectedMount {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var telescopeControlView: TelescopeControlView

    @Volatile private var position = Geoid.IERS2010.latLon(Angle.ZERO, Angle.ZERO, Distance.ZERO)

    val mounts
        get() = equipmentManager.attachedMounts

    val gps
        get() = equipmentManager.attachedGPSs

    fun initialize() {
        registerListener(this)
    }

    override fun onReset() {
        updateTitle()
        updateStatus()
    }

    override fun onChanged(prev: Mount?, device: Mount) {
        if (prev !== device) savePreferences()

        updateTitle()
        computePosition()
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged,
            is GuideOutputPulsingChanged -> updateStatus()
            is MountGeographicCoordinateChanged -> computePosition()
        }
    }

    private fun computePosition() {
        val mount = value ?: return
        position = Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)
    }

    fun openINDIPanelControl() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = value
    }

    fun openINDIPanelControl(gps: GPS) {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = gps
    }

    fun openTelescopeControlServer() {
        telescopeControlView.show(bringToFront = true)
    }

    fun openSiteAndTime() {
        val window = SiteAndTimeWindow(value ?: return, this)
        window.showAndWait()
    }

    fun park() {
        if (value.parked) {
            value.unpark()
        } else if (!value.parking) {
            value.park()
        }
    }

    fun goTo() {
        val (ra, dec) = try {
            view.targetCoordinates
        } catch (e: NumberFormatException) {
            return view.showAlert("Invalid target coordinates")
        }

        if (view.isJ2000) value.goToJ2000(ra, dec)
        else value.goTo(ra, dec)
    }

    fun slewTo() {
        val (ra, dec) = try {
            view.targetCoordinates
        } catch (e: NumberFormatException) {
            return view.showAlert("Invalid target coordinates")
        }

        if (view.isJ2000) value.slewToJ2000(ra, dec)
        else value.slewTo(ra, dec)
    }

    fun sync() {
        val (ra, dec) = try {
            view.targetCoordinates
        } catch (e: NumberFormatException) {
            return view.showAlert("Invalid target coordinates")
        }

        if (view.isJ2000) value.syncJ2000(ra, dec)
        else value.sync(ra, dec)
    }

    fun loadCurrentLocation() {
        view.updateTargetPosition(value.rightAscension, value.declination)
        view.isJ2000 = false
    }

    fun loadCurrentLocationJ2000() {
        view.updateTargetPosition(rightAscensionJ2000.hours, declinationJ2000.deg)
        view.isJ2000 = true
    }

    fun loadZenithLocation() {
        view.updateTargetPosition(computeLST(), value.latitude)
        view.isJ2000 = false
    }

    fun loadNorthCelestialPoleLocation() {
        view.updateTargetPosition(computeLST(), Angle.QUARTER)
        view.isJ2000 = false
    }

    fun loadSouthCelestialPoleLocation() {
        view.updateTargetPosition(computeLST(), -Angle.QUARTER)
        view.isJ2000 = false
    }

    fun loadGalacticCenterLocation() {
        val ra = Angle.from("17 45 40.04", true)!!
        val dec = Angle.from("−29 00 28.1")!!
        view.updateTargetPosition(ra, dec)
        view.isJ2000 = true
    }

    fun nudgeTo(direction: Char, enable: Boolean) {
        when (direction) {
            'N' -> value?.moveNorth(enable)
            'S' -> value?.moveSouth(enable)
            'W' -> value?.moveWest(enable)
            'E' -> value?.moveEast(enable)
        }
    }

    fun abort() {
        value.abortMotion()
    }

    fun toggleTrackingMode(mode: TrackMode) {
        value.trackingMode(mode)
    }

    fun updateTitle() {
        view.title = "Mount · $name"
    }

    fun updateStatus() {
        view.status = if (value == null) "idle"
        else if (value.parking) "parking"
        else if (value.parked) "parked"
        else if (value.slewing) "slewing"
        else if (value.tracking) "tracking"
        else if (value.pulseGuiding) "guiding"
        else "idle"
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferences.double("mount.screen.x", view.x)
        preferences.double("mount.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("mount.screen.x")?.also { view.x = it }
        preferences.double("mount.screen.y")?.also { view.y = it }
    }

    private fun computeLST(time: InstantOfTime = UTC.now()): Angle {
        return if (value == null) Angle.ZERO else position.lstAt(time)
    }

    private fun computeTimeLeftToMeridianFlip(): Angle {
        if (value == null) return Angle.ZERO
        val timeLeft = value.rightAscension - computeLST()
        return if (timeLeft.value < 0.0) timeLeft - SIDEREAL_TIME_DIFF * (timeLeft.normalized.value / TAU)
        else timeLeft + SIDEREAL_TIME_DIFF * (1.0 - timeLeft.value / TAU)
    }

    private fun computeHourAngle(): Angle {
        if (value == null) return Angle.ZERO
        return (computeLST() - value.rightAscension).normalized
    }

    @Scheduled(fixedRate = 1L, initialDelay = 1L, timeUnit = TimeUnit.SECONDS)
    fun onTimerHit() {
        if (value == null || !view.showing) return

        val lst = computeLST()
        val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip()
        val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

        javaFXExecutorService.execute {
            view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip)
        }
    }

    override fun close() {
        savePreferences()
    }

    companion object {

        private const val SIDEREAL_TIME_DIFF = 0.06552777 * PI / 12.0
    }
}
