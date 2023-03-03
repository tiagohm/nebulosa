package nebulosa.stellarium.protocol

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.math.Angle
import org.slf4j.LoggerFactory

class StellariumProtocolHandler(private val server: StellariumProtocolServer) : ChannelInboundHandlerAdapter(), CurrentPositionHandler {

    @Volatile private var client: ChannelHandlerContext? = null

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        client = ctx
        server.registerCurrentPositionHandler(this)
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
        val message = msg as StellariumProtocolMessage.Goto
        server.goTo(message.rightAscension, message.declination)
        ctx.close()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOG.error("stellarium protocol error", cause)
        ctx.close()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(StellariumProtocolHandler::class.java)
    }
}
