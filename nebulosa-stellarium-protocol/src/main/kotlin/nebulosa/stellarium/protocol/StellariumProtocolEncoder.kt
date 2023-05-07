package nebulosa.stellarium.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import nebulosa.constants.PI
import nebulosa.erfa.eraAnpm
import nebulosa.log.loggerFor

class StellariumProtocolEncoder : MessageToByteEncoder<StellariumProtocolMessage>() {

    override fun encode(
        ctx: ChannelHandlerContext,
        message: StellariumProtocolMessage,
        output: ByteBuf,
    ) {
        if (message is StellariumProtocolMessage.CurrentPosition) output.sendCurrentPosition(message)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<StellariumProtocolEncoder>()

        @JvmStatic
        private fun ByteBuf.sendCurrentPosition(message: StellariumProtocolMessage.CurrentPosition) {
            writeShortLE(24) // LENGTH
            writeShortLE(0) // TYPE
            writeLongLE(System.currentTimeMillis() * 1000L) // TIME
            writeIntLE((eraAnpm(message.rightAscension).value / PI * 0x80000000).toInt()) // RA
            writeIntLE((message.declination.value / PI * 0x80000000).toInt()) // DEC
            writeIntLE(0) // STATUS=OK

            if (LOG.isDebugEnabled) {
                LOG.debug("MessageCurrentPosition: ra={}, dec={}", message.rightAscension.hours, message.declination.degrees)
            }
        }
    }
}
