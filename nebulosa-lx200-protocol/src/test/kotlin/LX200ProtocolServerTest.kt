import nebulosa.lx200.protocol.LX200MountHandler
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import java.time.OffsetDateTime

class LX200ProtocolServerTest {

    companion object : LX200MountHandler {

        @JvmStatic
        fun main(args: Array<String>) {
            val server = LX200ProtocolServer(port = 10001)

            server.attachMountHandler(this@Companion)
            server.run()

            Thread.currentThread().join()
        }

        override var rightAscension = "05 15 07".hours

        override var declination = "25 26 03".deg

        override var latitude = 0.0

        override var longitude = 0.0

        override var slewing = false

        override var tracking = false

        override var parked = false

        override fun goTo(rightAscension: Angle, declination: Angle) {
            this.rightAscension = rightAscension
            this.declination = declination
        }

        override fun syncTo(rightAscension: Angle, declination: Angle) {
            this.rightAscension = rightAscension
            this.declination = declination
        }

        override fun moveNorth(enabled: Boolean) = Unit

        override fun moveSouth(enabled: Boolean) = Unit

        override fun moveWest(enabled: Boolean) = Unit

        override fun moveEast(enabled: Boolean) = Unit

        override fun time(time: OffsetDateTime) = Unit

        override fun coordinates(longitude: Angle, latitude: Angle) {
            this.longitude = longitude
            this.latitude = latitude
        }

        override fun abort() = Unit
    }
}
