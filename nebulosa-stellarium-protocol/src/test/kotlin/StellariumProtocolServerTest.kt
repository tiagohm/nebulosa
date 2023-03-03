import nebulosa.math.Angle
import nebulosa.stellarium.protocol.StellariumMountHandler
import nebulosa.stellarium.protocol.StellariumProtocolServer
import kotlin.concurrent.thread

class StellariumProtocolServerTest {

    companion object : StellariumMountHandler {

        override var rightAscension = Angle.from("05 15 07", true)!!
        override var declination = Angle.from("25 26 03")!!
        override var rightAscensionJ2000 = Angle.from("05 15 07", true)!!
        override var declinationJ2000 = Angle.from("25 26 03")!!

        @JvmStatic
        fun main(args: Array<String>) {
            val server = StellariumProtocolServer(port = 10002)

            server.attachMountHandler(this@Companion)

            thread(isDaemon = true) {
                while (true) {
                    Thread.sleep(1000L)
                    server.sendCurrentPosition(rightAscension, declination)
                }
            }

            server.run()

            Thread.currentThread().join()
        }

        override fun goTo(rightAscension: Angle, declination: Angle, j2000: Boolean) {
            if (j2000) {
                rightAscensionJ2000 = rightAscension
                declinationJ2000 = declination
            } else {
                this.rightAscension = rightAscension
                this.declination = declination
            }
        }
    }
}
