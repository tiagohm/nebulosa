package nebulosa.desktop.logic.telescopecontrol

import nebulosa.indi.device.mounts.Mount
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    val mount: Mount

    val isClosed: Boolean

    fun start()

    fun sendCurrentPosition() = Unit
}
