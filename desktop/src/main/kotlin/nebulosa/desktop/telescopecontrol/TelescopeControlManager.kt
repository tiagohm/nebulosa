package nebulosa.desktop.telescopecontrol

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TelescopeControlManager private constructor(private val servers: MutableMap<Mount, TelescopeControlServer>) :
    KoinComponent, Consumer<Any>, Map<Mount, TelescopeControlServer> by servers {

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
            // TODO: COORDINATES CHANGED. servers[mount]?.sendCurrentPosition()
        }
    }

    @Synchronized
    fun startTCP(
        mount: Mount,
        host: String,
        port: Int,
    ) {
        stop(mount)

        val server = TelescopeControlTCPServer(host, port)
        server.start()
        // TODO: Ignore if is slewing.
        // TODO: server.registerListener { ra, dec -> mount.goTo(ra, dec) }
        servers[mount] = server
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
}
