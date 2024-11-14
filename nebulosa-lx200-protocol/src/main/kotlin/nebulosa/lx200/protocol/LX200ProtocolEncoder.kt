package nebulosa.lx200.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.dms
import nebulosa.math.hms
import nebulosa.netty.writeAscii
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class LX200ProtocolEncoder : MessageToByteEncoder<LX200ProtocolMessage>() {

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: LX200ProtocolMessage,
        output: ByteBuf,
    ) {
        LOG.d { debug("sending message: {}", msg) }

        when (msg) {
            LX200ProtocolMessage.Ack -> output.writeByte(71)
            LX200ProtocolMessage.Ok -> output.writeByte(49)
            LX200ProtocolMessage.Zero -> output.writeByte(48)
            is LX200ProtocolMessage.Text -> output.writeAscii(msg.text)
            is LX200ProtocolMessage.RAPosition -> {
                val (h, m, s) = msg.rightAscension.hms()
                output.writeAscii("+%02d:%02d:%02d#".format(Locale.ENGLISH, h.toInt(), m.toInt(), s.toInt()))
            }
            is LX200ProtocolMessage.DECPosition -> {
                val (d, m, s) = msg.declination.dms()
                val sign = if (d < 0.0) "-" else "+"
                output.writeAscii("%s%02d*%02d:%02d#".format(Locale.ENGLISH, sign, abs(d).toInt(), m.toInt(), s.toInt()))
            }
            is LX200ProtocolMessage.Longitude -> {
                // East is negative.
                val (d, m) = (-msg.longitude).dms()
                val sign = if (d < 0.0) "-" else "+"
                output.writeAscii("%s%03d*%02d#".format(Locale.ENGLISH, sign, abs(d).toInt(), m.toInt()))
            }
            is LX200ProtocolMessage.Latitude -> {
                val (d, m) = msg.latitude.dms()
                val sign = if (d < 0.0) "-" else "+"
                output.writeAscii("%s%02d*%02d#".format(Locale.ENGLISH, sign, abs(d).toInt(), m.toInt()))
            }
            is LX200ProtocolMessage.Date -> {
                val date = msg.date.format(CALENDAR_DATE_FORMAT)
                output.writeAscii("$date#")
            }
            is LX200ProtocolMessage.Time -> {
                val time = msg.time.format(CALENDAR_TIME_FORMAT)
                output.writeAscii("$time#")
            }
            is LX200ProtocolMessage.ZoneOffset -> {
                val sign = if (msg.offset >= 0) "-" else "+"
                output.writeAscii("%s%04.01f#".format(Locale.ENGLISH, sign, abs(msg.offset)))
            }
            is LX200ProtocolMessage.Slewing -> {
                output.writeAscii(if (msg.slewing) "|#" else "#")
            }
            is LX200ProtocolMessage.Status -> {
                val b = if (msg.tracking) "T" else "N"
                val c = if (msg.parked) "P" else "H"
                output.writeAscii("%s%s%s#".format(Locale.ENGLISH, msg.type, b, c))
            }
        }
    }

    companion object {

        private val LOG = loggerFor<LX200ProtocolEncoder>()

        internal val CALENDAR_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy")
        internal val CALENDAR_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
