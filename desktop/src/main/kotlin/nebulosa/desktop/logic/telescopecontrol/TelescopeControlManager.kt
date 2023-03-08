package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.telescopecontrol.TelescopeControlType
import nebulosa.desktop.view.telescopecontrol.TelescopeControlView
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEquatorialCoordinatesChanged
import nebulosa.indi.device.mount.MountEquatorialJ2000CoordinatesChanged
import nebulosa.indi.device.mount.MountEvent
import nebulosa.lx200.protocol.LX200MountHandler
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.math.Angle
import nebulosa.stellarium.protocol.StellariumMountHandler
import nebulosa.stellarium.protocol.StellariumProtocolServer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Closeable
import java.time.OffsetDateTime

@Component
class TelescopeControlManager(@Autowired internal val view: TelescopeControlView) :
    Closeable, LX200MountHandler, StellariumMountHandler {

    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var preferences: Preferences

    @Volatile private var stellariumProtocolServer: StellariumProtocolServer? = null
    @Volatile private var lx200ProtocolServer: LX200ProtocolServer? = null

    val mount: Mount?
        get() = equipmentManager.selectedMount.value

    fun initialize() {
        eventBus.register(this)
    }

    @Subscribe
    fun onEvent(event: MountEvent) {
        if (event.device !== mount) return

        when (event) {
            is MountEquatorialCoordinatesChanged -> {
                val server = stellariumProtocolServer ?: return
                if (!server.j2000) server.sendCurrentPosition(event.device.rightAscension, event.device.declination)
            }
            is MountEquatorialJ2000CoordinatesChanged -> {
                val server = stellariumProtocolServer ?: return
                if (server.j2000) server.sendCurrentPosition(event.device.rightAscensionJ2000, event.device.declinationJ2000)
            }
        }
    }

    fun updateConnectionStatus() {
        val server = if (view.type == TelescopeControlType.LX200) lx200ProtocolServer
        else stellariumProtocolServer

        if (server == null) {
            val host = preferences.string("telescopeControl.${view.type}.host") ?: ""
            val port = preferences.int("telescopeControl.${view.type}.port") ?: 0
            view.updateConnectionStatus(false, host, port)
        } else {
            view.updateConnectionStatus(server.running, server.host, server.port)
        }
    }

    fun connect() {
        var server = if (view.type == TelescopeControlType.LX200) lx200ProtocolServer
        else stellariumProtocolServer

        if (server != null && server.running) {
            server.close()
        } else {
            server = when (view.type) {
                TelescopeControlType.LX200 -> {
                    LX200ProtocolServer(view.host, view.port).also {
                        it.attachMountHandler(this)
                        lx200ProtocolServer = it
                    }
                }
                TelescopeControlType.STELLARIUM_J2000,
                TelescopeControlType.STELLARIUM_JNOW -> {
                    val j2000 = view.type == TelescopeControlType.STELLARIUM_J2000
                    StellariumProtocolServer(view.host, view.port, j2000).also {
                        it.attachMountHandler(this)
                        stellariumProtocolServer = it
                    }
                }
            }

            server.run()

            preferences.string("telescopeControl.${view.type}.host", server.host)
            preferences.int("telescopeControl.${view.type}.port", server.port)
        }

        updateConnectionStatus()
    }

    override fun close() {
        eventBus.unregister(this)

        stellariumProtocolServer?.close()
        lx200ProtocolServer?.close()

        stellariumProtocolServer = null
        lx200ProtocolServer = null
    }

    override val rightAscension
        get() = mount?.rightAscension ?: Angle.ZERO

    override val declination
        get() = mount?.declination ?: Angle.ZERO

    override val rightAscensionJ2000
        get() = mount?.rightAscensionJ2000 ?: Angle.ZERO

    override val declinationJ2000
        get() = mount?.declinationJ2000 ?: Angle.ZERO

    override val latitude
        get() = mount?.latitude ?: Angle.ZERO

    override val longitude
        get() = mount?.longitude ?: Angle.ZERO

    override val slewing
        get() = mount?.slewing ?: false

    override val tracking
        get() = mount?.tracking ?: false

    override val parked
        get() = mount?.parked ?: false

    override fun goTo(rightAscension: Angle, declination: Angle) {
        mount?.goToJ2000(rightAscension, declination)
    }

    override fun goTo(rightAscension: Angle, declination: Angle, j2000: Boolean) {
        if (j2000) mount?.goToJ2000(rightAscension, declination)
        else mount?.goTo(rightAscension, declination)
    }

    override fun syncTo(rightAscension: Angle, declination: Angle) {
        mount?.syncJ2000(rightAscension, declination)
    }

    override fun moveNorth(enable: Boolean) {
        mount?.moveNorth(enable)
    }

    override fun moveSouth(enable: Boolean) {
        mount?.moveSouth(enable)
    }

    override fun moveWest(enable: Boolean) {
        mount?.moveWest(enable)
    }

    override fun moveEast(enable: Boolean) {
        mount?.moveEast(enable)
    }

    override fun time(time: OffsetDateTime) {
        mount?.time(time)
    }

    override fun coordinates(longitude: Angle, latitude: Angle) {
        mount?.coordinates(longitude, latitude, mount!!.elevation)
    }

    override fun abort() {
        mount?.abortMotion()
    }
}
