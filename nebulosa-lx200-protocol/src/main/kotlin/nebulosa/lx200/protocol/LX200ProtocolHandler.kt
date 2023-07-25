package nebulosa.lx200.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import java.time.*
import java.util.concurrent.atomic.AtomicBoolean

class LX200ProtocolHandler(private val server: LX200ProtocolServer) : ChannelInboundHandlerAdapter() {

    private val started = AtomicBoolean()

    @Volatile private var rightAscension = Angle.ZERO
    @Volatile private var declination = Angle.ZERO
    @Volatile private var date = LocalDate.now()
    @Volatile private var time = LocalTime.now()
    @Volatile private var offset = ZoneId.systemDefault().rules.getOffset(Instant.now())

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        LOG.info("client connected. address={}", ctx.channel().remoteAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        LOG.info("client disconnected. address={}", ctx.channel().remoteAddress())
    }

    private fun ChannelHandlerContext.updateRA(text: String) {
        rightAscension = Angle.from(text, true)
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    private fun ChannelHandlerContext.updateDEC(text: String) {
        declination = Angle.from(text)
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val input = msg as ByteBuf

        if (input.readableBytes() < 2) return

        val command = input.toString(Charsets.US_ASCII)

        LOG.debug { "command received. command=$command" }

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
                "#:GW#" -> ctx.writeAndFlush(LX200ProtocolMessage.Status("G", server.tracking, server.parked))
                "#:CM#" -> {
                    ctx.writeAndFlush(LX200ProtocolMessage.Zero)
                    server.syncTo(rightAscension, declination)
                }
                "#:MS#" -> {
                    ctx.writeAndFlush(LX200ProtocolMessage.Zero)
                    server.goTo(rightAscension, declination)
                }
                // "#:RC#", "#:RG#", "#:RM#", "#:RS#" -> return // movement rate
                "#:Me#", "#:Mn#", "#:Ms#", "#:Mw#" -> {
                    when (command[3]) {
                        'n' -> server.moveNorth(true)
                        's' -> server.moveSouth(true)
                        'w' -> server.moveWest(true)
                        'e' -> server.moveEast(true)
                    }
                }
                "#:Qe#", "#:Qn#", "#:Qs#", "#:Qw#" -> {
                    when (command[3]) {
                        'n' -> server.moveNorth(false)
                        's' -> server.moveSouth(false)
                        'w' -> server.moveWest(false)
                        'e' -> server.moveEast(false)
                    }
                }
                "#:Q#" -> server.abort()
                "#:D#" -> ctx.writeAndFlush(LX200ProtocolMessage.Slewing(server.slewing))
                else -> {
                    when {
                        command.startsWith("#:Sg") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            val longitude = -Angle.from(command.substring(4))
                            server.coordinates(longitude, server.latitude)
                        }
                        command.startsWith("#:St") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            val latitude = Angle.from(command.substring(4))
                            server.coordinates(server.longitude, latitude)
                        }
                        command.startsWith("#:SL") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            time = LocalTime.parse(command.substring(4, command.length - 1), LX200ProtocolEncoder.CALENDAR_TIME_FORMAT)
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:SC") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Text("1Updating planetary data       #                              #"))
                            date = LocalDate.parse(command.substring(4, command.length - 1), LX200ProtocolEncoder.CALENDAR_DATE_FORMAT)
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:SG") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            val offsetInHours = -command.substring(4, command.length - 1).toDouble()
                            offset = ZoneOffset.ofTotalSeconds((offsetInHours * 3600.0).toInt())
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:Sr") -> ctx.updateRA(command.substring(4))
                        command.startsWith("#:Sd") -> ctx.updateDEC(command.substring(4))
                        else -> LOG.warn("received unknown command. command={}", command)
                    }
                }
            }
        } else {
            if (command == "#\u0006") {
                ctx.writeAndFlush(LX200ProtocolMessage.Ack)
                started.set(true)
                LOG.info("LX200 protocol handling started")
            }
        }

        input.release()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<LX200ProtocolHandler>()
    }
}
