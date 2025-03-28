package nebulosa.stellarium.protocol

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.log.loggerFor
import nebulosa.math.Angle

internal class StellariumProtocolHandler(private val server: StellariumProtocolServer) : ChannelInboundHandlerAdapter(), CurrentPositionHandler {

    @Volatile private var client: ChannelHandlerContext? = null

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        client = ctx
        server.registerCurrentPositionHandler(this)
        sendCurrentPosition(server.rightAscension, server.declination)
        LOG.info("client connected. address={}", ctx.channel().remoteAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        server.unregisterCurrentPositionHandler(this)
        client = null
        LOG.info("client disconnected. address={}", ctx.channel().remoteAddress())
    }

    @Synchronized
    override fun sendCurrentPosition(rightAscension: Angle, declination: Angle) {
        client?.writeAndFlush(StellariumProtocolMessage.CurrentPosition(rightAscension, declination))
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is StellariumProtocolMessage.Goto -> server.goTo(msg.rightAscension, msg.declination)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.error("stellarium protocol error", cause)
        ctx.close()
    }

    companion object {

        private val LOG = loggerFor<StellariumProtocolHandler>()
    }
}
