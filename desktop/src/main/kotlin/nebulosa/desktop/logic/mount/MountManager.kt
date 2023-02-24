package nebulosa.desktop.logic.mount

import jakarta.annotation.PostConstruct
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.gui.mount.SiteAndTimeWindow
import nebulosa.desktop.gui.telescopecontrol.TelescopeControlWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlLX200Server
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlStellariumServer
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.mount.MountView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
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
    @Autowired internal val equipmentManager: EquipmentManager,
) : MountProperty by equipmentManager.selectedMount {

    @Autowired private lateinit var preferences: Preferences
    @Autowired internal lateinit var indiPanelControlWindow: INDIPanelControlWindow

    val mounts
        get() = equipmentManager.attachedMounts

    @PostConstruct
    private fun initialize() {
        registerListener(this)
    }

    override fun onReset() {
        updateTitle()
        updateStatus()
    }

    override fun onChanged(prev: Mount?, device: Mount) {
        if (prev !== device) savePreferences()

        updateTitle()

        TelescopeControlStellariumServer.mount = device
        TelescopeControlLX200Server.mount = device
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged,
            is GuideOutputPulsingChanged -> updateStatus()
            is MountEquatorialCoordinatesChanged -> TelescopeControlStellariumServer.sendCurrentPosition()
        }
    }

    fun openINDIPanelControl() {
        indiPanelControlWindow.show(bringToFront = true)
        indiPanelControlWindow.device = value
    }

    fun openTelescopeControlServer() {
        val window = TelescopeControlWindow(this)
        window.showAndWait()
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

    fun loadCurrentPostion() {
        view.updateTargetPosition(value.rightAscension, value.declination)
        view.isJ2000 = false
    }

    fun loadCurrentPostionJ2000() {
        view.updateTargetPosition(rightAscensionJ2000.hours, declinationJ2000.deg)
        view.isJ2000 = true
    }

    fun loadZenithPosition() {
        view.updateTargetPosition(computeLST(), value.latitude)
        view.isJ2000 = false
    }

    fun loadNorthCelestialPolePosition() {
        view.updateTargetPosition(computeLST(), Angle.QUARTER)
        view.isJ2000 = false
    }

    fun loadSouthCelestialPolePosition() {
        view.updateTargetPosition(computeLST(), -Angle.QUARTER)
        view.isJ2000 = false
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
        preferences.double("mount.screen.x", view.x)
        preferences.double("mount.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("mount.screen.x")?.also { view.x = it }
        preferences.double("mount.screen.y")?.also { view.y = it }
    }

    private fun computeLST(time: InstantOfTime = UTC.now()): Angle {
        if (value == null) return Angle.ZERO
        val position = Geoid.IERS2010.latLon(value.longitude, value.latitude, value.elevation)
        return position.lstAt(time)
    }

    private fun computeTimeLeftToMeridianFlip(): Angle {
        if (value == null) return Angle.ZERO
        return value.rightAscension - computeLST()
    }

    @Scheduled(fixedRate = 1L, initialDelay = 1L, timeUnit = TimeUnit.SECONDS)
    fun onTimerHit() {
        if (value == null || !view.showing) return

        val lst = computeLST()
        val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip()
        val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

        javaFxThread {
            view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip)
            computeCoordinates()
        }
    }

    override fun close() {
        savePreferences()

        TelescopeControlStellariumServer.close()
        TelescopeControlLX200Server.close()
    }
}
