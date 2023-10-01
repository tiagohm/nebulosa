import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.stellarium.protocol.StellariumMountHandler
import nebulosa.stellarium.protocol.StellariumProtocolServer
import kotlin.concurrent.thread

class StellariumProtocolServerTest {

    companion object : StellariumMountHandler {

        override var rightAscension = "05 15 07".hours
        override var declination = "25 26 03".deg
        override var rightAscensionJ2000 = "05 15 07".hours
        override var declinationJ2000 = "25 26 03".deg

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
