package nebulosa.stellarium.protocol

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.math.Angle
import nebulosa.netty.NettyServer
import java.util.concurrent.atomic.AtomicReference

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
class StellariumProtocolServer(
    override val host: String = "0.0.0.0",
    override val port: Int = 10001,
    val j2000: Boolean = false,
) : NettyServer(), CurrentPositionHandler {

    private val stellariumMountHandler = AtomicReference<StellariumMountHandler>()
    private val currentPositionHandlers = LinkedHashSet<CurrentPositionHandler>()

    val rightAscension: Angle?
        get() = stellariumMountHandler.get()?.rightAscension

    val declination: Angle?
        get() = stellariumMountHandler.get()?.declination

    val rightAscensionJ2000: Angle?
        get() = stellariumMountHandler.get()?.rightAscensionJ2000

    val declinationJ2000: Angle?
        get() = stellariumMountHandler.get()?.declinationJ2000

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                StellariumProtocolDecoder(),
                StellariumProtocolEncoder(),
                StellariumProtocolHandler(this@StellariumProtocolServer),
            )
        }
    }

    @Synchronized
    override fun sendCurrentPosition(rightAscension: Angle, declination: Angle) {
        currentPositionHandlers.forEach { it.sendCurrentPosition(rightAscension, declination) }
    }

    internal fun registerCurrentPositionHandler(handler: CurrentPositionHandler) {
        currentPositionHandlers.add(handler)
    }

    internal fun unregisterCurrentPositionHandler(handler: CurrentPositionHandler) {
        currentPositionHandlers.remove(handler)
    }

    fun attachMountHandler(handler: StellariumMountHandler) {
        stellariumMountHandler.set(handler)
    }

    fun detachMountHandler() {
        stellariumMountHandler.set(null)
    }

    @Synchronized
    internal fun goTo(rightAscension: Angle, declination: Angle) {
        stellariumMountHandler.get()?.goTo(rightAscension, declination, j2000)
    }

    override fun close() {
        stellariumMountHandler.set(null)
        currentPositionHandlers.clear()

        super.close()
    }
}
