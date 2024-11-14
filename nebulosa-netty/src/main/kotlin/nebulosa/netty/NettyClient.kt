package nebulosa.netty

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.concurrent.atomic.AtomicReference

abstract class NettyClient : AutoCloseable {

    protected val channel = AtomicReference<ChannelFuture>()

    protected abstract val channelInitialzer: ChannelInitializer<SocketChannel>

    val isOpen
        get() = channel.get() != null

    fun open(host: String, port: Int) {
        require(!isOpen) { "the server has already been started" }

        val masterGroup = NioEventLoopGroup()

        val b = Bootstrap()

        b.group(masterGroup)
            .channel(NioSocketChannel::class.java)
            .handler(channelInitialzer)
            .option(ChannelOption.TCP_NODELAY, true)

        val future = b.connect(host, port).sync()

        channel.set(future)

        LOG.d { info("{} is running. host={}, port={}", this::class.simpleName, host, port) }

        future.channel().closeFuture().addListener {
            masterGroup.shutdownGracefully()

            channel.set(null)

            LOG.d { info("{} closed", this::class.simpleName) }
        }
    }

    override fun close() {
        channel.get()?.channel()?.close()?.sync()
    }

    companion object {

        private val LOG = loggerFor<NettyClient>()
    }
}
