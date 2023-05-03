package nebulosa.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

abstract class NettyServer : Runnable, Closeable {

    protected val channel = AtomicReference<ChannelFuture>()

    abstract val host: String

    abstract val port: Int

    protected abstract val channelInitialzer: ChannelInitializer<SocketChannel>

    val running
        get() = channel.get() != null

    final override fun run() {
        require(!running) { "the server has already been started" }

        val masterGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        val b = ServerBootstrap()

        b.group(masterGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(channelInitialzer)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)

        // Bind and start to accept incoming connections.
        val future = b.bind(host, port).sync()

        channel.set(future)

        LOG.info("server is running. host={}, port={}", host, port)

        future.channel().closeFuture().addListener {
            workerGroup.shutdownGracefully()
            masterGroup.shutdownGracefully()

            channel.set(null)

            LOG.info("server is closed")
        }
    }

    override fun close() {
        channel.get()?.channel()?.close()?.sync()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(NettyServer::class.java)
    }
}
