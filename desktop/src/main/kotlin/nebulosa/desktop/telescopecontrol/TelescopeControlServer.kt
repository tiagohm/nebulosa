package nebulosa.desktop.telescopecontrol

import nebulosa.math.Angle
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    fun interface MessageListener {

        fun onGoTo(ra: Angle, dec: Angle)
    }

    val isClosed: Boolean

    fun registerListener(listener: MessageListener)

    fun unregisterListener(listener: MessageListener)

    fun start()

    fun sendCurrentPosition(ra: Angle, dec: Angle)
}
