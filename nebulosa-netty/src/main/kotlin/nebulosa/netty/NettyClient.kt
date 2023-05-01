package nebulosa.netty

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

abstract class NettyClient : Runnable, Closeable {

    protected val channel = AtomicReference<ChannelFuture>()

    abstract val host: String

    abstract val port: Int

    protected abstract val channelInitialzer: ChannelInitializer<SocketChannel>

    val running
        get() = channel.get() != null

    final override fun run() {
        require(!running) { "the server has already been started" }

        val masterGroup = NioEventLoopGroup()

        val b = Bootstrap()

        b.group(masterGroup)
            .channel(NioSocketChannel::class.java)
            .handler(channelInitialzer)
            .option(ChannelOption.SO_BACKLOG, 128)
            .option(ChannelOption.TCP_NODELAY, true)

        val future = b.connect(host, port).sync()

        channel.set(future)

        LOG.info("client is running. host={}, port={}", host, port)

        future.channel().closeFuture().addListener {
            masterGroup.shutdownGracefully()

            channel.set(null)

            LOG.info("client is closed")
        }
    }

    override fun close() {
        channel.get()?.channel()?.close()?.sync()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(NettyClient::class.java)
    }
}
