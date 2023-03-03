import nebulosa.math.Angle
import nebulosa.stellarium.protocol.StellariumProtocolServer
import kotlin.concurrent.thread

class StellariumProtocolServerTest {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val server = StellariumProtocolServer(port = 10002)
            var rightAscension = Angle.from("05 15 07", true)!!
            var declination = Angle.from("25 26 03")!!

            server.registerGoToHandler { ra, dec ->
                rightAscension = ra
                declination = dec
            }

            thread(isDaemon = true) {
                while (true) {
                    Thread.sleep(1000L)
                    server.sendCurrentPosition(rightAscension, declination)
                }
            }

            server.run()

            Thread.currentThread().join()
        }
    }
}
