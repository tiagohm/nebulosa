package nebulosa.stellarium.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import nebulosa.constants.PI
import nebulosa.erfa.eraAnpm
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.toDegrees
import nebulosa.math.toHours

class StellariumProtocolEncoder : MessageToByteEncoder<StellariumProtocolMessage>() {

    override fun encode(
        ctx: ChannelHandlerContext,
        message: StellariumProtocolMessage,
        output: ByteBuf,
    ) {
        if (message is StellariumProtocolMessage.CurrentPosition) output.sendCurrentPosition(message)
    }

    companion object {

        private val LOG = loggerFor<StellariumProtocolEncoder>()

        private fun ByteBuf.sendCurrentPosition(message: StellariumProtocolMessage.CurrentPosition) {
            writeShortLE(24) // LENGTH
            writeShortLE(0) // TYPE
            writeLongLE(System.currentTimeMillis() * 1000L) // TIME
            writeIntLE((eraAnpm(message.rightAscension) / PI * 0x80000000).toInt()) // RA
            writeIntLE((message.declination / PI * 0x80000000).toInt()) // DEC
            writeIntLE(0) // STATUS=OK

            LOG.d("MessageCurrentPosition: ra={}, dec={}", message.rightAscension.toHours, message.declination.toDegrees)
        }
    }
}
