package nebulosa.desktop.logic.telescopecontrol

import nebulosa.math.Angle
import java.io.Closeable

interface TelescopeControlServer : Closeable {

    interface Telescope {

        val rightAscension: Angle

        val declination: Angle

        val rightAscensionJ2000: Angle

        val declinationJ2000: Angle

        val longitude: Angle

        val latitude: Angle

        val slewing: Boolean

        fun goTo(ra: Angle, dec: Angle, j2000: Boolean = true)

        fun sync(ra: Angle, dec: Angle, j2000: Boolean = true)

        fun abort()
    }

    val running: Boolean

    val host: String

    val port: Int

    var telescope: Telescope?

    fun start(host: String, port: Int)

    fun sendCurrentPosition() = Unit
}
