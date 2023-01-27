package nebulosa.desktop.logic.mount

import javafx.application.Platform
import nebulosa.desktop.gui.mount.MountView
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.desktop.telescopecontrol.TelescopeControlServerScreen
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guiders.GuiderPulsingChanged
import nebulosa.indi.device.mounts.*
import nebulosa.math.Angle
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

    override fun onChanged(prev: Mount?, device: Mount) {}

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {
        when (event) {
            is MountParkChanged,
            is MountTrackingChanged,
            is MountSlewingChanged,
            is GuiderPulsingChanged -> updateStatus()
        }
    }

    fun openTelescopeControlServer() {
        val screen = TelescopeControlServerScreen(value ?: return)
        screen.showAndWait()
    }

    fun park() {
        if (value.isParked) {
            value.unpark()
        } else if (!value.isParking) {
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
        view.updateTargetPosition(value.rightAscensionJ2000, value.declinationJ2000)
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
        else if (value.isParking) "parking"
        else if (value.isParked) "parked"
        else if (value.isSlewing) "slewing"
        else if (value.isTracking) "tracking"
        else if (value.isPulseGuiding) "guiding"
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
        if (value == null || !view.isShowing) return

        val lst = computeLST()
        val timeLeftToMeridianFlip = computeTimeLeftToMeridianFlip()
        val timeToMeridianFlip = LocalDateTime.now().plusSeconds((timeLeftToMeridianFlip.hours * 3600.0).toLong())

        Platform.runLater { view.updateLSTAndMeridian(lst, timeLeftToMeridianFlip, timeToMeridianFlip) }
    }

    override fun close() {
        savePreferences()
    }
}
