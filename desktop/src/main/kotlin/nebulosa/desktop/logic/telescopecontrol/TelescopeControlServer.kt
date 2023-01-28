package nebulosa.desktop.logic.telescopecontrol

import nebulosa.indi.device.mounts.Mount
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    val running: Boolean

    val host: String

    val port: Int

    fun attach(mount: Mount)

    fun detach()

    fun start(host: String, port: Int)

    fun sendCurrentPosition() = Unit

    companion object {

        @JvmStatic val SERVERS = mapOf<TelescopeControlServerType, TelescopeControlServer>(
            TelescopeControlServerType.STELLARIUM to TelescopeControlStellariumServer,
            TelescopeControlServerType.LX200 to TelescopeControlLX200Server,
        )
    }
}
