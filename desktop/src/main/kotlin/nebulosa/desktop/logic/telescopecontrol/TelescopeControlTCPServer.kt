package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.logic.io.TCPServer
import nebulosa.indi.device.mounts.Mount

abstract class TelescopeControlTCPServer : TCPServer(), TelescopeControlServer {

    @Volatile protected var mount: Mount? = null

    override fun attach(mount: Mount) {
        this.mount = mount
        sendCurrentPosition()
    }

    override fun detach() {
        this.mount = null
    }
}
