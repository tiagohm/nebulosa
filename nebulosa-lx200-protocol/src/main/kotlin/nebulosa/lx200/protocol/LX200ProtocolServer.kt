package nebulosa.lx200.protocol

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import nebulosa.netty.NettyServer
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicReference

/**
 * Meade Telescope Serial Command Protocol.
 *
 * @see <a href="http://www.company7.com/library/meade/LX200CommandSet.pdf">Meade Telescope Serial Command Protocol</a>
 */
data class LX200ProtocolServer(
    override val host: String = "0.0.0.0",
    override val port: Int = 10001,
) : NettyServer(), LX200MountHandler {

    private val mountHandler = AtomicReference<LX200MountHandler>()

    override val rightAscension
        get() = mountHandler.get()?.rightAscension ?: 0.0

    override val declination
        get() = mountHandler.get()?.declination ?: 0.0

    override val latitude
        get() = mountHandler.get()?.latitude ?: 0.0

    override val longitude
        get() = mountHandler.get()?.longitude ?: 0.0

    override val slewing
        get() = mountHandler.get()?.slewing == true

    override val tracking
        get() = mountHandler.get()?.tracking == true

    override val parked
        get() = mountHandler.get()?.parked == true

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                LX200ProtocolEncoder(),
                LX200ProtocolHandler(this@LX200ProtocolServer),
            )
        }
    }

    fun attachMountHandler(handler: LX200MountHandler) {
        require(handler !== this) { "cannot attach this server" }
        mountHandler.set(handler)
    }

    fun detachMountHandler() {
        mountHandler.set(null)
    }

    @Synchronized
    override fun goTo(rightAscension: Angle, declination: Angle) {
        LOG.d { info("going to. ra={}, dec={}", rightAscension.toHours, declination.toDegrees) }
        mountHandler.get()?.goTo(rightAscension, declination)
    }

    @Synchronized
    override fun syncTo(rightAscension: Angle, declination: Angle) {
        LOG.d { info("syncing to. ra={}, dec={}", rightAscension.toHours, declination.toDegrees) }
        mountHandler.get()?.syncTo(rightAscension, declination)
    }

    @Synchronized
    override fun moveNorth(enabled: Boolean) {
        LOG.d { info("moving to north. enabled={}", enabled) }
        mountHandler.get()?.moveNorth(enabled)
    }

    @Synchronized
    override fun moveSouth(enabled: Boolean) {
        LOG.d { info("moving to south. enabled={}", enabled) }
        mountHandler.get()?.moveSouth(enabled)
    }

    @Synchronized
    override fun moveWest(enabled: Boolean) {
        LOG.d { info("moving to west. enabled={}", enabled) }
        mountHandler.get()?.moveWest(enabled)
    }

    @Synchronized
    override fun moveEast(enabled: Boolean) {
        LOG.d { info("moving to east. enabled={}", enabled) }
        mountHandler.get()?.moveEast(enabled)
    }

    @Synchronized
    override fun time(time: OffsetDateTime) {
        LOG.d { info("sending time. time={}", time) }
        mountHandler.get()?.time(time)
    }

    @Synchronized
    override fun coordinates(longitude: Angle, latitude: Angle) {
        LOG.d { info("sending coordinates. longitude={}, latitude={}", longitude.toDegrees, latitude.toDegrees) }
        mountHandler.get()?.coordinates(longitude, latitude)
    }

    @Synchronized
    override fun abort() {
        LOG.d { info("aborting") }
        mountHandler.get()?.abort()
    }

    override fun close() {
        mountHandler.set(null)

        super.close()
    }

    companion object {

        private val LOG = loggerFor<LX200ProtocolServer>()
    }
}
