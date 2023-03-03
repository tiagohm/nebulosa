import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.lx200.protocol.MountHandler
import nebulosa.math.Angle

class LX200ProtocolServerTest {

    companion object : MountHandler {

        @JvmStatic
        fun main(args: Array<String>) {
            val server = LX200ProtocolServer(port = 10001)

            server.attachMountHandler(this@Companion)

            server.run()

            Thread.currentThread().join()
        }

        override var rightAscension = Angle.from("05 15 07", true)!!

        override var declination = Angle.from("25 26 03")!!

        override val latitude = Angle.ZERO

        override val longitude = Angle.ZERO

        override var slewing = false

        override var tracking = false

        override fun goTo(rightAscension: Angle, declination: Angle) {
            this.rightAscension = rightAscension
            this.declination = declination
        }

        override fun syncTo(rightAscension: Angle, declination: Angle) {}

        override fun abort() {}
    }
}
