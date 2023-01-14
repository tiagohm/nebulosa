package nebulosa.desktop.telescopecontrol

import nebulosa.indi.devices.mounts.Mount
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    val mount: Mount

    val isClosed: Boolean

    fun start()

    fun sendCurrentPosition() = Unit
}
