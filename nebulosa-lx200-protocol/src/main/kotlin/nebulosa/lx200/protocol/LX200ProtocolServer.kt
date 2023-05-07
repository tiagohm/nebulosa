package nebulosa.lx200.protocol

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.netty.NettyServer
import java.time.OffsetDateTime
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

    private val mountHandler = AtomicReference<LX200MountHandler>()

    val rightAscension
        get() = mountHandler.get()?.rightAscensionJ2000 ?: Angle.ZERO

    val declination
        get() = mountHandler.get()?.declinationJ2000 ?: Angle.ZERO

    val latitude
        get() = mountHandler.get()?.latitude ?: Angle.ZERO

    val longitude
        get() = mountHandler.get()?.longitude ?: Angle.ZERO

    val slewing
        get() = mountHandler.get()?.slewing ?: false

    val tracking
        get() = mountHandler.get()?.tracking ?: false

    val parked
        get() = mountHandler.get()?.parked ?: false

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                LX200ProtocolEncoder(),
                LX200ProtocolHandler(this@LX200ProtocolServer),
            )
        }
    }

    fun attachMountHandler(handler: LX200MountHandler) {
        mountHandler.set(handler)
    }

    fun detachMountHandler() {
        mountHandler.set(null)
    }

    @Synchronized
    internal fun goTo(rightAscension: Angle, declination: Angle) {
        LOG.info("going to. ra={}, dec={}", rightAscension.hours, declination.degrees)
        mountHandler.get()?.goTo(rightAscension, declination)
    }

    @Synchronized
    internal fun syncTo(rightAscension: Angle, declination: Angle) {
        LOG.info("syncing to. ra={}, dec={}", rightAscension.hours, declination.degrees)
        mountHandler.get()?.syncTo(rightAscension, declination)
    }

    @Synchronized
    internal fun moveNorth(enable: Boolean) {
        LOG.info("moving to north. enable={}", enable)
        mountHandler.get()?.moveNorth(enable)
    }

    @Synchronized
    internal fun moveSouth(enable: Boolean) {
        LOG.info("moving to south. enable={}", enable)
        mountHandler.get()?.moveSouth(enable)
    }

    @Synchronized
    internal fun moveWest(enable: Boolean) {
        LOG.info("moving to west. enable={}", enable)
        mountHandler.get()?.moveWest(enable)
    }

    @Synchronized
    internal fun moveEast(enable: Boolean) {
        LOG.info("moving to east. enable={}", enable)
        mountHandler.get()?.moveEast(enable)
    }

    @Synchronized
    internal fun time(time: OffsetDateTime) {
        LOG.info("sending time. time={}", time)
        mountHandler.get()?.time(time)
    }

    @Synchronized
    internal fun coordinates(longitude: Angle, latitude: Angle) {
        LOG.info("sending coordinates. longitude={}, latitude={}", longitude.degrees, latitude.degrees)
        mountHandler.get()?.coordinates(longitude, latitude)
    }

    @Synchronized
    internal fun abort() {
        LOG.info("aborting")
        mountHandler.get()?.abort()
    }

    override fun close() {
        mountHandler.set(null)

        super.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<LX200ProtocolServer>()
    }
}
