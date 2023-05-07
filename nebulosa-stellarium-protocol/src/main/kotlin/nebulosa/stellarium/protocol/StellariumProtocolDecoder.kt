package nebulosa.stellarium.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import nebulosa.constants.PI
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle.Companion.rad

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

        LOG.debug { "MessageGoto: ra=%f, dec=%f".format(rightAscension.hours, declination.degrees) }

        output.add(StellariumProtocolMessage.Goto(rightAscension, declination))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<StellariumProtocolDecoder>()
    }
}
