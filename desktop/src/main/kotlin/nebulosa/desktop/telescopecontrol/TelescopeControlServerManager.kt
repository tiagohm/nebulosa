package nebulosa.desktop.telescopecontrol

import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountDetached
import nebulosa.indi.devices.mounts.MountEquatorialCoordinatesChanged
import nebulosa.indi.devices.mounts.MountEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class TelescopeControlServerManager : KoinComponent {

    private val eventBus by inject<EventBus>()

    private val servers = HashMap<Mount, LinkedList<TelescopeControlServer>>()

    init {
        eventBus
            .filterIsInstance<MountEvent> { it.device in servers }
            .subscribe(::onMountEvent)
    }

    private fun onMountEvent(event: MountEvent) {
        when (event) {
            is MountDetached -> stopAll(event.device)
            is MountEquatorialCoordinatesChanged -> {
                servers[event.device]?.forEach { it.sendCurrentPosition() }
            }
        }
    }

    inline fun <reified T : TelescopeControlServer> get(mount: Mount) = get(mount, T::class.java)

    fun <T : TelescopeControlServer> get(mount: Mount, type: Class<T>): TelescopeControlServer? {
        return servers[mount]?.firstOrNull { type === it::class.java }
    }

    inline fun <reified T : TelescopeControlServer> isClosed(mount: Mount) = isClosed(mount, T::class.java)

    fun <T : TelescopeControlServer> isClosed(mount: Mount, type: Class<T>): Boolean {
        return get(mount, type)?.isClosed ?: true
    }

    fun startStellariumServer(
        mount: Mount,
        host: String, port: Int,
    ): TelescopeControlServer {
        stop<TelescopeControlStellariumServer>(mount)

        val server = TelescopeControlStellariumServer(mount, host, port)
        server.start()

        servers
            .getOrPut(mount) { LinkedList() }
            .add(server)

        return server
    }

    fun startLX200Server(
        mount: Mount,
        host: String, port: Int,
    ): TelescopeControlServer {
        stop<TelescopeControlLX200Server>(mount)

        val server = TelescopeControlLX200Server(mount, host, port)
        server.start()

        servers
            .getOrPut(mount) { LinkedList() }
            .add(server)

        return server
    }

    inline fun <reified T : TelescopeControlServer> stop(mount: Mount) = stop(mount, T::class.java)

    fun <T : TelescopeControlServer> stop(mount: Mount, type: Class<T>) {
        val server = get(mount, type) ?: return
        server.close()
        servers[mount]!!.remove(server)
        if (servers[mount]!!.isEmpty()) servers.remove(mount)
    }

    fun stopAll(mount: Mount) {
        servers[mount]?.forEach(TelescopeControlServer::close)
        servers[mount]?.clear()
        servers.remove(mount)
    }

    fun stopAll() {
        servers.values.forEach { it.forEach(TelescopeControlServer::close) }
        servers.clear()
    }
}
