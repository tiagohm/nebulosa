package nebulosa.desktop.telescopecontrol

import nebulosa.indi.devices.mounts.Mount
import nebulosa.math.Angle
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    fun interface CommandListener {

        fun onGoTo(server: TelescopeControlServer, ra: Angle, dec: Angle, isJ2000: Boolean)
    }

    val mount: Mount

    val isClosed: Boolean

    fun registerCommandListener(listener: CommandListener)

    fun unregisterCommandListener(listener: CommandListener)

    fun start()

    fun sendCurrentPosition(ra: Angle, dec: Angle)
}
