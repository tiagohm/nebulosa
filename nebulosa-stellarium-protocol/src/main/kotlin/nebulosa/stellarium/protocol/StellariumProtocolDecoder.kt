package nebulosa.stellarium.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import nebulosa.constants.PI
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.normalized
import nebulosa.math.rad
import nebulosa.math.toDegrees
import nebulosa.math.toHours

internal class StellariumProtocolDecoder : ByteToMessageDecoder() {

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        output: MutableList<Any>,
    ) {
        if (input.readableBytes() < 20) return

        input.readShortLE() // LENGTH
        input.readShortLE() // TYPE
        input.readLongLE() // TIME

        val rightAscension = (input.readIntLE() * (PI / 0x80000000)).rad.normalized
        val declination = (input.readIntLE() * (PI / 0x80000000)).rad

        LOG.d { debug("MessageGoto: ra={}, dec={}", rightAscension.toHours, declination.toDegrees) }

        output.add(StellariumProtocolMessage.Goto(rightAscension, declination))
    }

    companion object {

        private val LOG = loggerFor<StellariumProtocolDecoder>()
    }
}
