package nebulosa.desktop.telescopecontrol

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountEquatorialCoordinatesChanged
import nebulosa.indi.devices.mounts.MountEvent
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TelescopeControlManager private constructor(private val servers: MutableMap<Mount, TelescopeControlServer>) :
    KoinComponent, Consumer<Any>, TelescopeControlServer.Listener, Map<Mount, TelescopeControlServer> by servers {

    constructor() : this(HashMap<Mount, TelescopeControlServer>())

    private val eventBus by inject<EventBus>()

    init {
        eventBus
            .filter { it is MountEvent }
            .subscribe(this)
    }

    @Synchronized
    override fun accept(event: Any) {
        when (event) {
            is MountEquatorialCoordinatesChanged -> servers[event.device]
                ?.sendCurrentPosition(event.device.rightAscension.hours, event.device.declination.deg)
        }
    }

    @Synchronized
    fun startTCP(
        mount: Mount,
        host: String,
        port: Int,
    ): TelescopeControlServer {
        stop(mount)

        val server = TelescopeControlTCPServer(mount, host, port)
        server.start()
        server.registerListener(this)
        servers[mount] = server

        return server
    }

    @Synchronized
    fun stop(mount: Mount) {
        servers[mount]?.close()
        servers.remove(mount)
    }

    @Synchronized
    fun stopAll() {
        servers.values.forEach(TelescopeControlServer::close)
        servers.clear()
    }

    override fun onGoTo(mount: Mount, ra: Angle, dec: Angle) {
        if (!mount.isSlewing) {
            mount.goToJ2000(ra, dec)
        }
    }
}
