package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.logic.io.TCPServer
import nebulosa.indi.device.mount.Mount

abstract class TelescopeControlTCPServer : TCPServer(), TelescopeControlServer {

    override var mount: Mount? = null
}
