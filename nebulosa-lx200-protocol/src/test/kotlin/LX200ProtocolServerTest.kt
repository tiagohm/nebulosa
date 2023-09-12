import nebulosa.lx200.protocol.LX200MountHandler
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
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

        override var rightAscensionJ2000 = "05 15 07".hours

        override var declinationJ2000 = "25 26 03".deg

        override val latitude = Angle.ZERO

        override val longitude = Angle.ZERO

        override var slewing = false

        override var tracking = false

        override var parked = false

        override fun goTo(rightAscension: Angle, declination: Angle) {
            this.rightAscensionJ2000 = rightAscension
            this.declinationJ2000 = declination
        }

        override fun syncTo(rightAscension: Angle, declination: Angle) {
            this.rightAscensionJ2000 = rightAscension
            this.declinationJ2000 = declination
        }

        override fun moveNorth(enable: Boolean) {}

        override fun moveSouth(enable: Boolean) {}

        override fun moveWest(enable: Boolean) {}

        override fun moveEast(enable: Boolean) {}

        override fun time(time: OffsetDateTime) {}

        override fun coordinates(longitude: Angle, latitude: Angle) {}

        override fun abort() {}
    }
}
