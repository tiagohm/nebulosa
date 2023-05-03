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
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.TimeJD
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

    @Volatile private var timeUTC = UTC.now()
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
        device.computePosition()
        device.computeCoordinates(epoch = timeUTC)
    }

    override suspend fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged,
            is GuideOutputPulsingChanged -> updateStatus()
            is MountEquatorialCoordinatesChanged -> device.computeCoordinates(epoch = timeUTC)
            is MountGeographicCoordinateChanged -> device.computePosition()
        }
    }

    private fun Mount.computePosition() {
        position = Geoid.IERS2010.latLon(longitude, latitude, elevation)
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
        val rightAscension = view.targetRightAscension
        val declination = view.targetDeclination

        if (rightAscension.valid && declination.valid) {
            if (view.isJ2000) value.goToJ2000(rightAscension, declination)
            else value.goTo(rightAscension, declination)
        } else {
            return view.showAlert("Invalid target coordinates")
        }
    }

    fun slewTo() {
        val rightAscension = view.targetRightAscension
        val declination = view.targetDeclination

        if (rightAscension.valid && declination.valid) {
            if (view.isJ2000) value.slewToJ2000(rightAscension, declination)
            else value.slewTo(rightAscension, declination)
        } else {
            return view.showAlert("Invalid target coordinates")
        }
    }

    fun sync() {
        val rightAscension = view.targetRightAscension
        val declination = view.targetDeclination

        if (rightAscension.valid && declination.valid) {
            if (view.isJ2000) value.syncJ2000(rightAscension, declination)
            else value.sync(rightAscension, declination)
        } else {
            return view.showAlert("Invalid target coordinates")
        }
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
        view.updateTargetPosition(GALACTIC_CENTER_RA, GALACTIC_CENTER_DEC)
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

        preferenceService.double("mount.screen.x", max(0.0, view.x))
        preferenceService.double("mount.screen.y", max(0.0, view.y))
    }

    fun loadPreferences() {
        preferenceService.double("mount.screen.x")?.also { view.x = it }
        preferenceService.double("mount.screen.y")?.also { view.y = it }
    }

    private suspend fun computeLST(time: UTC = timeUTC) = withIO {
        position.lstAt(time)
    }

    private suspend fun computeTimeLeftToMeridianFlip(rightAscension: Angle, lst: Angle) = withIO {
        val timeLeft = rightAscension - lst
        if (timeLeft.value < 0.0) timeLeft - SIDEREAL_TIME_DIFF * (timeLeft.normalized.value / TAU)
        else timeLeft + SIDEREAL_TIME_DIFF * (1.0 - timeLeft.value / TAU)
    }

    @Scheduled(fixedRate = 1L, initialDelay = 1L, timeUnit = TimeUnit.SECONDS)
    fun onTimerHit() {
        val mount = value ?: return

        if (!mount.connected) return
        if (!view.showing) return

        timeUTC = UTC.now()

        launch {
            val lst = computeLST()
            val now = LocalDateTime.now()
            val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip(mount.rightAscension, lst)
            val timeToMeridianFlip = now.plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

            if (mount.tracking) {
                mount.computeCoordinates(j2000 = false, epoch = timeUTC)
            }

            view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip)

            runCatching {
                val targetRA = view.targetRightAscension
                val targetDEC = view.targetDeclination

                if (!targetRA.valid || !targetDEC.valid) return@runCatching

                val epoch = if (view.isJ2000) TimeJD.J2000 else timeUTC
                val icrf = ICRF.equatorial(targetRA, targetDEC, time = timeUTC, epoch = epoch, center = position)
                val targetConstellation = Constellation.find(icrf)

                val horizontal = icrf.horizontal()
                val targetAzimuth = horizontal.longitude.normalized
                val targetAltitude = horizontal.latitude

                val targetTimeLeftToMeridianFlip = computeTimeLeftToMeridianFlip(targetRA, lst)
                val targetTimeToMeridianFlip = now.plusSeconds((targetTimeLeftToMeridianFlip.hours * 3600.0).toLong())

                view.updateTargetInfo(targetAzimuth, targetAltitude, targetConstellation, targetTimeLeftToMeridianFlip, targetTimeToMeridianFlip)
            }
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

        @JvmStatic private val GALACTIC_CENTER_RA = Angle.from("17 45 40.04", true)!!
        @JvmStatic private val GALACTIC_CENTER_DEC = Angle.from("−29 00 28.1")!!
    }
}
