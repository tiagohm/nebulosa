package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.telescopecontrol.TelescopeControlType
import nebulosa.desktop.view.telescopecontrol.TelescopeControlView
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEquatorialCoordinatesChanged
import nebulosa.indi.device.mount.MountEquatorialJ2000CoordinatesChanged
import nebulosa.indi.device.mount.MountEvent
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.lx200.protocol.MountHandler
import nebulosa.math.Angle
import nebulosa.stellarium.protocol.GoToHandler
import nebulosa.stellarium.protocol.StellariumProtocolServer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Closeable

@Component
class TelescopeControlManager(@Autowired private val view: TelescopeControlView) :
    Closeable, MountHandler, GoToHandler {

    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var equipmentManager: EquipmentManager

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

        if (server == null) view.updateConnectionStatus(false, "", 0)
        else view.updateConnectionStatus(server.running, server.host, server.port)
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
                        it.registerGoToHandler(this)
                        stellariumProtocolServer = it
                    }
                }
            }

            server.run()
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
        get() = mount?.rightAscensionJ2000 ?: Angle.ZERO

    override val declination
        get() = mount?.declinationJ2000 ?: Angle.ZERO

    override val latitude
        get() = mount?.latitude ?: Angle.ZERO

    override val longitude
        get() = mount?.longitude ?: Angle.ZERO

    override val slewing
        get() = mount?.slewing ?: false

    override val tracking
        get() = mount?.tracking ?: false

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

    override fun abort() {
        mount?.abortMotion()
    }
}
