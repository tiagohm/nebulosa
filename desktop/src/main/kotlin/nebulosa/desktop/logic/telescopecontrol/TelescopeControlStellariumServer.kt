package nebulosa.desktop.logic.telescopecontrol

import nebulosa.constants.PI
import nebulosa.erfa.eraAnpm
import nebulosa.math.Angle.Companion.rad
import org.slf4j.LoggerFactory
import java.net.Socket

/**
 * Stellarium Telescope Protocol version 1.0
 *
 * The Stellarium Telescope Protocol works on top of TCP/IP.
 * The client is the stellarium program (or any similar program),
 * the server is called "telescope server" and controls the telescope.
 * Depending on the implementation the server may handle one or many
 * simultaneous clients. The reference server implementation accepts
 * an unlimited number of simultaneous clients.
 *
 * The protocol is message based: both server and client may
 * spontaneousely send messages as often as they want.
 * When the server looses control of the telescope he should
 * actively close the connection to all clients.
 *
 * The first two bytes of any message describes the total
 * length of that message in bytes (including the first 2 bytes).
 *
 * The next 2 bytes of any message describe the type of this message.
 *
 * The byte order for all kind of integers is always LSB.
 *
 * ```text
 * server->client:
 * MessageCurrentPosition (type = 0):
 *
 * LENGTH (2 bytes,integer): length of the message
 * TYPE   (2 bytes,integer): 0
 * TIME   (8 bytes,integer): current time on the server computer in microseconds
 *            since 1970.01.01 UT. Currently unused.
 * RA     (4 bytes,unsigned integer): right ascension of the telescope (J2000)
 *            a value of 0x100000000 = 0x0 means 24h=0h,
 *            a value of 0x80000000 means 12h
 * DEC    (4 bytes,signed integer): declination of the telescope (J2000)
 *            a value of -0x40000000 means -90degrees,
 *            a value of 0x0 means 0degrees,
 *            a value of 0x40000000 means 90degrees
 * STATUS (4 bytes,signed integer): status of the telescope, currently unused.
 *            status=0 means ok, status<0 means some error
 *
 * client->server:
 * MessageGoto (type =0)
 * LENGTH (2 bytes,integer): length of the message
 * TYPE   (2 bytes,integer): 0
 * TIME   (8 bytes,integer): current time on the client computer in microseconds
 *                   since 1970.01.01 UT. Currently unused.
 * RA     (4 bytes,unsigned integer): right ascension of the telescope (J2000)
 *            a value of 0x100000000 = 0x0 means 24h=0h,
 *            a value of 0x80000000 means 12h
 * DEC    (4 bytes,signed integer): declination of the telescope (J2000)
 *            a value of -0x40000000 means -90degrees,
 *            a value of 0x0 means 0degrees,
 *            a value of 0x40000000 means 90degrees
 * ```
 *
 * @see <a href="https://free-astro.org/images/b/b7/Stellarium_telescope_protocol.txt">Protocol</a>
 * @see <a href="https://github.com/Stellarium/stellarium/blob/master/plugins/TelescopeControl/src/TelescopeClient.cpp">Stellarium Implementation</a>
 */
object TelescopeControlStellariumServer : TelescopeControlTCPServer() {

    override fun sendCurrentPosition() {
        forEach { (it as TelescopeClient).sendCurrentPosition() }
    }

    override fun acceptSocket(socket: Socket): Client = TelescopeClient(this, socket)

    private class TelescopeClient(
        val server: TelescopeControlStellariumServer,
        socket: Socket,
    ) : Client(socket) {

        fun sendCurrentPosition() {
            val mount = server.mount ?: return
            val ra = mount.rightAscensionJ2000
            val dec = mount.declinationJ2000

            output.writeShortLe(24) // LENGTH
            output.writeShortLe(0) // TYPE
            output.writeLongLe(System.currentTimeMillis() * 1000L) // TIME
            output.writeIntLe((eraAnpm(ra).value / PI * 0x80000000).toInt()) // RA
            output.writeIntLe((dec.value / PI * 0x80000000).toInt()) // DEC
            output.writeIntLe(0) // STATUS=OK
            output.flush()

            if (LOG.isDebugEnabled) {
                LOG.debug("MessageCurrentPosition: ra=${ra.hours}, dec=${dec.degrees}")
            }
        }

        override fun start() {
            super.start()

            sendCurrentPosition()
        }

        override fun processMessage(): Boolean {
            val readCount = input.read(buffer, 20L)

            if (buffer.size >= 20L) {
                buffer.readShortLe() // LENGTH
                buffer.readShortLe() // TYPE
                buffer.readLongLe() // TIME
                val ra = (buffer.readIntLe() * (PI / 0x80000000)).rad.normalized
                val dec = (buffer.readIntLe() * (PI / 0x80000000)).rad
                if (LOG.isDebugEnabled) LOG.debug("MessageGoto: ra=${ra.hours}, dec=${dec.degrees}")
                server.mount?.goToJ2000(ra, dec)
            } else if (readCount < 0L) {
                return false
            }

            return true
        }
    }

    @JvmStatic private val LOG = LoggerFactory.getLogger(TelescopeControlStellariumServer::class.java)
}
