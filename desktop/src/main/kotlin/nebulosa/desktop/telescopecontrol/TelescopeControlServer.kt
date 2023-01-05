package nebulosa.desktop.telescopecontrol

import nebulosa.indi.devices.mounts.Mount
import nebulosa.math.Angle
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    fun interface Listener {

        fun onGoTo(mount: Mount, ra: Angle, dec: Angle)
    }

    val isClosed: Boolean

    fun registerListener(listener: Listener)

    fun unregisterListener(listener: Listener)

    fun start()

    fun sendCurrentPosition(ra: Angle, dec: Angle)
}
