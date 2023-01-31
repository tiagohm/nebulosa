package nebulosa.desktop.logic.mount

import javafx.application.Platform
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.gui.mount.SiteAndTimeWindow
import nebulosa.desktop.gui.telescopecontrol.TelescopeControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlLX200Server
import nebulosa.desktop.logic.telescopecontrol.TelescopeControlStellariumServer
import nebulosa.desktop.view.mount.MountView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guider.GuiderPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.nova.position.Geoid
import nebulosa.time.InstantOfTime
import nebulosa.time.TimeJD
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.timer

class MountManager(private val view: MountView) :
    MountProperty by GlobalContext.get().get<EquipmentManager>().selectedMount, KoinComponent {

    private val preferences by inject<Preferences>()
    private val equipmentManager by inject<EquipmentManager>()

    @JvmField val mounts = equipmentManager.attachedMounts

    private val timer = timer(daemon = true, period = 1000L, action = ::onTimerHit)

    init {
        registerListener(this)
    }

    override fun onReset() {
        updateTitle()
        updateStatus()
    }

    override fun onChanged(prev: Mount?, device: Mount) {
        if (prev !== device) savePreferences()

        TelescopeControlStellariumServer.mount = device
        TelescopeControlLX200Server.mount = device
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged,
            is GuiderPulsingChanged -> updateStatus()
            is MountEquatorialCoordinatesChanged -> TelescopeControlStellariumServer.sendCurrentPosition()
        }
    }

    fun openINDIPanelControl() {
        INDIPanelControlWindow.open(value)
    }

    fun openTelescopeControlServer() {
        val window = TelescopeControlWindow(this)
        window.showAndWait()
    }

    fun openSiteAndTime() {
        val window = SiteAndTimeWindow(value ?: return)
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
        view.title = if (value == null) "Mount" else "Mount · $name"
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
        preferences.double("value.screen.x", view.x)
        preferences.double("value.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("value.screen.x")?.also { view.x = it }
        preferences.double("value.screen.y")?.also { view.y = it }
    }

    private fun computeLST(time: InstantOfTime = TimeJD.now()): Angle {
        if (value == null) return Angle.ZERO
        val position = Geoid.IERS2010.latLon(value.longitude, value.latitude, value.elevation)
        return position.lstAt(time)
    }

    private fun computeTimeLeftToMeridianFlip(): Angle {
        if (value == null) return Angle.ZERO
        return value.rightAscension - computeLST(TimeJD.now())
    }

    private fun onTimerHit(task: TimerTask) {
        if (value == null || !view.showing) return

        val lst = computeLST()
        val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip()
        val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

        Platform.runLater {
            view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip)
            computeCoordinates()
        }
    }

    override fun close() {
        savePreferences()

        timer.cancel()

        TelescopeControlStellariumServer.close()
        TelescopeControlLX200Server.close()
    }
}
