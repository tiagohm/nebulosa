package nebulosa.lx200.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.math.Angle
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

class LX200ProtocolHandler(private val server: LX200ProtocolServer) : ChannelInboundHandlerAdapter() {

    private val started = AtomicBoolean()

    @Volatile private var rightAscension = Angle.ZERO
    @Volatile private var declination = Angle.ZERO

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        LOG.info("client connected. address={}", ctx.channel().remoteAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        LOG.info("client disconnected. address={}", ctx.channel().remoteAddress())
    }

    private fun ChannelHandlerContext.updateRA(text: String) {
        rightAscension = Angle.from(text, true)!!
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    private fun ChannelHandlerContext.updateDEC(text: String) {
        declination = Angle.from(text)!!
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val input = msg as ByteBuf

        if (input.readableBytes() < 2) return

        val command = input.toString(Charsets.US_ASCII)

        if (LOG.isDebugEnabled) {
            LOG.debug("command received. command={}", command)
        }

        if (started.get()) {
            when (command) {
                "#\u0006" -> ctx.writeAndFlush(LX200ProtocolMessage.Ack)
                "#:GR#" -> ctx.writeAndFlush(LX200ProtocolMessage.RAPosition(server.rightAscension))
                "#:GD#" -> ctx.writeAndFlush(LX200ProtocolMessage.DECPosition(server.declination))
                "#:Gg#" -> ctx.writeAndFlush(LX200ProtocolMessage.Longitude(server.longitude))
                "#:Gt#" -> ctx.writeAndFlush(LX200ProtocolMessage.Latitude(server.latitude))
                "#:GC#" -> ctx.writeAndFlush(LX200ProtocolMessage.Date(LocalDate.now()))
                "#:GL#" -> ctx.writeAndFlush(LX200ProtocolMessage.Time(LocalTime.now()))
                "#:GG#" -> ctx.writeAndFlush(LX200ProtocolMessage.ZoneOffset(ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 3600.0))
                "#:GW#" -> ctx.writeAndFlush(LX200ProtocolMessage.Status("G", server.tracking)) // A = AltAz, G = German
                "#:CM#" -> {
                    server.goTo(rightAscension, declination)
                    ctx.writeAndFlush(LX200ProtocolMessage.Zero)
                }
                "#:MS#" -> {
                    server.syncTo(rightAscension, declination)
                    ctx.writeAndFlush(LX200ProtocolMessage.Zero)
                }
                // "#:RC#", "#:RG#", "#:RM#", "#:RS#" -> return // movement rate
                // "#:Me#", "#:Mn#", "#:Ms#", "#:Mw#" -> return // move
                // "#:Qe#", "#:Qn#", "#:Qs#", "#:Qw#" -> return // abort move
                "#:Q#" -> server.abort()
                "#:D#" -> ctx.writeAndFlush(LX200ProtocolMessage.Slewing(server.slewing))
                else -> {
                    when {
                        command.startsWith("#:Sg") -> ctx.writeAndFlush(LX200ProtocolMessage.Ok) // Longitude
                        command.startsWith("#:St") -> ctx.writeAndFlush(LX200ProtocolMessage.Ok) // Latitude
                        command.startsWith("#:SL") -> ctx.writeAndFlush(LX200ProtocolMessage.Ok) // Local Time
                        command.startsWith("#:SC") -> ctx.writeAndFlush(LX200ProtocolMessage.Ok) // Calendar Date
                        command.startsWith("#:SG") -> ctx.writeAndFlush(LX200ProtocolMessage.Ok) // Time Offset
                        command.startsWith("#:Sr") -> return ctx.updateRA(command.substring(4))
                        command.startsWith("#:Sd") -> return ctx.updateDEC(command.substring(4))
                        else -> LOG.warn("received unknown command. command={}", command)
                    }
                }
            }
        } else {
            if (command == "#\u0006") {
                started.set(true)
                ctx.writeAndFlush(LX200ProtocolMessage.Ack)
                LOG.info("LX200 protocol handling started")
            }
        }

        input.release()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(LX200ProtocolHandler::class.java)
    }
}
