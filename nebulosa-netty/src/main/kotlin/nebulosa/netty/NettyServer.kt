package nebulosa.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.concurrent.atomic.AtomicReference

abstract class NettyServer : Runnable, AutoCloseable {

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

        LOG.d { info("{} is running. host={}, port={}", this::class.simpleName, host, port) }

        future.channel().closeFuture().addListener {
            workerGroup.shutdownGracefully()
            masterGroup.shutdownGracefully()

            channel.set(null)

            LOG.d { info("{} is closed", this::class.simpleName) }
        }
    }

    override fun close() {
        channel.get()?.channel()?.close()?.sync()
    }

    companion object {

        private val LOG = loggerFor<NettyServer>()
    }
}
