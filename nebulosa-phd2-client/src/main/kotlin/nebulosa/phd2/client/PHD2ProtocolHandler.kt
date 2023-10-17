package nebulosa.phd2.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.phd2.client.events.PHD2Event

class PHD2ProtocolHandler(private val client: PHD2Client) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val event = msg as PHD2Event
        client.listeners.forEach { it.onEventReceived(event) }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        cause?.printStackTrace()
        ctx.close()
    }
}
