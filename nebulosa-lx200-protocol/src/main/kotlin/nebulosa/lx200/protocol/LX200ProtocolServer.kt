package nebulosa.lx200.protocol

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.math.Angle
import nebulosa.netty.NettyServer
import java.util.concurrent.atomic.AtomicReference

/**
 * Meade Telescope Serial Command Protocol.
 *
 * @see <a href="http://www.company7.com/library/meade/LX200CommandSet.pdf">Meade Telescope Serial Command Protocol</a>
 */
class LX200ProtocolServer(
    override val host: String = "0.0.0.0",
    override val port: Int = 10001,
) : NettyServer() {

    private val mountHandler = AtomicReference<MountHandler>()

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                LX200ProtocolEncoder(),
                LX200ProtocolHandler(this@LX200ProtocolServer),
            )
        }
    }

    fun attachMountHandler(handler: MountHandler) {
        mountHandler.set(handler)
    }

    fun detachMountHandler() {
        mountHandler.set(null)
    }

    val rightAscension
        get() = mountHandler.get()?.rightAscension ?: Angle.ZERO

    val declination
        get() = mountHandler.get()?.declination ?: Angle.ZERO

    val latitude
        get() = mountHandler.get()?.latitude ?: Angle.ZERO

    val longitude
        get() = mountHandler.get()?.longitude ?: Angle.ZERO

    val slewing
        get() = mountHandler.get()?.slewing ?: false

    val tracking
        get() = mountHandler.get()?.tracking ?: false

    @Synchronized
    internal fun goTo(rightAscension: Angle, declination: Angle) {
        mountHandler.get()?.goTo(rightAscension, declination)
    }

    @Synchronized
    internal fun syncTo(rightAscension: Angle, declination: Angle) {
        mountHandler.get()?.syncTo(rightAscension, declination)
    }

    @Synchronized
    internal fun abort() {
        mountHandler.get()?.abort()
    }

    override fun close() {
        mountHandler.set(null)

        super.close()
    }
}
