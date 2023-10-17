package nebulosa.phd2.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.phd2.client.commands.CompletableCommand
import nebulosa.phd2.client.commands.PHD2Command
import java.util.*

class PHD2ProtocolEncoder(private val mapper: ObjectMapper) : MessageToByteEncoder<PHD2Command<*>>() {

    override fun encode(ctx: ChannelHandlerContext, msg: PHD2Command<*>, out: ByteBuf) {
        val id = if (msg is CompletableCommand) msg.id else UUID.randomUUID().toString()
        val data = mapOf("method" to msg.methodName, "params" to msg.params, "id" to id)
        val bytes = mapper.writeValueAsBytes(data)
        LOG.debug { bytes.decodeToString() }
        out.writeBytes(bytes)
        out.writeByte(13)
        out.writeByte(10)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PHD2ProtocolEncoder>()
    }
}
