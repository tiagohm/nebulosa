package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.logic.io.TCPServer

abstract class TelescopeControlTCPServer : TCPServer(), TelescopeControlServer {

    override var telescope: TelescopeControlServer.Telescope? = null
}
