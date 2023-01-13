package nebulosa.desktop.telescopecontrol

import nebulosa.desktop.tcp.TCPServer
import nebulosa.indi.devices.mounts.Mount

abstract class TelescopeControlTCPServer(
    override val mount: Mount,
    host: String = "0.0.0.0",
    port: Int = 10001,
) : TCPServer(host, port), TelescopeControlServer {

    internal val commandListeners = HashSet<TelescopeControlServer.CommandListener>(1)

    final override fun registerCommandListener(listener: TelescopeControlServer.CommandListener) {
        commandListeners.add(listener)
    }

    final override fun unregisterCommandListener(listener: TelescopeControlServer.CommandListener) {
        commandListeners.remove(listener)
    }
}
