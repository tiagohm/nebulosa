package nebulosa.desktop.logic.telescopecontrol

import nebulosa.indi.device.mount.Mount
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    val mount: Mount?

    val running: Boolean

    val host: String

    val port: Int

    fun start(host: String, port: Int)

    fun sendCurrentPosition() = Unit
}
