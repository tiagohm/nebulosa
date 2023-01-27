package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.tcp.TCPServer
import nebulosa.indi.device.mounts.Mount

abstract class TelescopeControlTCPServer(
    override val mount: Mount,
    host: String = "0.0.0.0",
    port: Int = 10001,
) : TCPServer(host, port), TelescopeControlServer
