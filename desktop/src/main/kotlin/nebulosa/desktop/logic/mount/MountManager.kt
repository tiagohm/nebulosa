package nebulosa.desktop.logic.mount

import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.desktop.gui.mount.SiteAndTimeWindow
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
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
import kotlin.math.max

@Component
class MountManager(
    @Autowired internal val view: MountView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : AbstractManager(), MountProperty by equipmentManager.selectedMount {

    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView
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

    suspend fun openINDIPanelControl() {
        indiPanelControlView.show(value)
    }

    suspend fun openINDIPanelControl(gps: GPS) {
        indiPanelControlView.show(gps)
    }

    suspend fun openTelescopeControlServer() {
        telescopeControlView.show(bringToFront = true)
    }

    fun openSiteAndTime() {
        val window = SiteAndTimeWindow(value ?: return, this)
        window.showAndWait(view)
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

    suspend fun loadCurrentLocation() = withMain {
        view.updateTargetPosition(value.rightAscension, value.declination)
        view.isJ2000 = false
    }

    suspend fun loadCurrentLocationJ2000() = withMain {
        view.updateTargetPosition(rightAscensionJ2000.hours, declinationJ2000.deg)
        view.isJ2000 = true
    }

    suspend fun loadZenithLocation() = withMain {
        view.updateTargetPosition(computeLST(), value.latitude)
        view.isJ2000 = false
    }

    suspend fun loadNorthCelestialPoleLocation() = withMain {
        view.updateTargetPosition(computeLST(), Angle.QUARTER)
        view.isJ2000 = false
    }

    suspend fun loadSouthCelestialPoleLocation() = withMain {
        view.updateTargetPosition(computeLST(), -Angle.QUARTER)
        view.isJ2000 = false
    }

    suspend fun loadGalacticCenterLocation() = withMain {
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
        value?.abortMotion()
    }

    fun toggleTracking(enable: Boolean) {
        value?.tracking(enable)
    }

    fun toggleTrackingMode(mode: TrackMode) {
        value?.trackingMode(mode)
    }

    fun toggleSlewRate(rate: String) {
        value?.slewRate(rate)
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

        preferences.double("mount.screen.x", max(0.0, view.x))
        preferences.double("mount.screen.y", max(0.0, view.y))
    }

    fun loadPreferences() {
        preferences.double("mount.screen.x")?.also { view.x = it }
        preferences.double("mount.screen.y")?.also { view.y = it }
    }

    private suspend fun computeLST(time: InstantOfTime = UTC.now()) = withIO {
        if (value == null) Angle.ZERO else position.lstAt(time)
    }

    private suspend fun computeTimeLeftToMeridianFlip() = withIO {
        if (value == null) Angle.ZERO
        else {
            val timeLeft = value.rightAscension - computeLST()
            if (timeLeft.value < 0.0) timeLeft - SIDEREAL_TIME_DIFF * (timeLeft.normalized.value / TAU)
            else timeLeft + SIDEREAL_TIME_DIFF * (1.0 - timeLeft.value / TAU)
        }
    }

    @Scheduled(fixedRate = 1L, initialDelay = 1L, timeUnit = TimeUnit.SECONDS)
    fun onTimerHit() {
        val mount = value ?: return

        if (!mount.connected) return
        if (!view.showing) return

        launch {
            val lst = computeLST()
            val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip()
            val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

            if (mount.tracking) {
                mount.computeCoordinates(j2000 = false)
            }

            view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip)
        }
    }

    override fun close() {
        savePreferences()

        for (direction in "NSWE") {
            nudgeTo(direction, false)
        }
    }

    companion object {

        private const val SIDEREAL_TIME_DIFF = 0.06552777 * PI / 12.0
    }
}
