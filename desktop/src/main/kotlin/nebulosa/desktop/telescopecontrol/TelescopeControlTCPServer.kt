package nebulosa.desktop.telescopecontrol

import nebulosa.constants.PI
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import java.net.InetSocketAddress
import java.net.ServerSocket
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
class TelescopeControlTCPServer(
    val host: String = "0.0.0.0",
    val port: Int = 10001,
) : TelescopeControlServer {

    private val serverSocket = ServerSocket()
    private val acceptorThread = Thread(::acceptSocket)
    private val clients = ArrayList<TelescopeClient>(1)
    private val listeners = HashSet<TelescopeControlServer.MessageListener>(1)

    @Volatile private var closed = false

    override val isClosed get() = serverSocket.isClosed || closed

    override fun registerListener(listener: TelescopeControlServer.MessageListener) {
        listeners.add(listener)
    }

    override fun unregisterListener(listener: TelescopeControlServer.MessageListener) {
        listeners.remove(listener)
    }

    @Synchronized
    override fun start() {
        check(!isClosed) { "closed" }
        serverSocket.bind(InetSocketAddress(host, port))
        acceptorThread.start()
    }

    override fun close() {
        if (closed) return

        acceptorThread.interrupt()
        clients.forEach(Thread::interrupt)
        clients.clear()
        listeners.clear()

        try {
            serverSocket.close()
        } finally {
            closed = true
        }
    }

    override fun sendCurrentPosition(ra: Angle, dec: Angle) {
        clients.forEach { it.sendCurrentPosition(ra, dec) }
    }

    private fun acceptSocket() {
        try {
            while (true) {
                val socket = serverSocket.accept() ?: continue
                socket.keepAlive = true

                val client = TelescopeClient(socket)
                clients.add(client)
                client.start()

                println("Telescope TCP Server new client: $socket")
            }
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private inner class TelescopeClient(socket: Socket) : Thread() {

        private val input = socket.getInputStream().source().buffer()
        private val output = socket.getOutputStream().sink().buffer()

        fun sendCurrentPosition(ra: Angle, dec: Angle) {
            output.writeShortLe(24) // LENGTH
            output.writeShortLe(0) // TYPE
            output.writeLongLe(System.currentTimeMillis() * 1000L) // TIME
            output.writeIntLe((ra.value / PI * 0x80000000).toInt()) // RA
            output.writeIntLe((dec.value / PI * 0x80000000).toInt()) // DEC
            output.writeIntLe(0) // STATUS=OK
            output.flush()
        }

        override fun run() {
            val buffer = Buffer()

            try {
                while (true) {
                    val readCount = input.read(buffer, 20L)

                    if (buffer.size >= 20L) {
                        buffer.readShortLe() // LENGTH
                        buffer.readShortLe() // TYPE
                        buffer.readLongLe() // TIME
                        val ra = (buffer.readIntLe() * (PI / 0x80000000)).rad
                        val dec = (buffer.readIntLe() * (PI / 0x80000000)).rad
                        listeners.forEach { it.onGoTo(ra, dec) }
                    } else if (readCount < 0L) {
                        break
                    }
                }
            } catch (_: InterruptedException) {
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                clients.remove(this)
            }
        }
    }
}
