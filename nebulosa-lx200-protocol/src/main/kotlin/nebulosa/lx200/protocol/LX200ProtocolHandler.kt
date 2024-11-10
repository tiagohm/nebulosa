package nebulosa.lx200.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import nebulosa.log.*
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.time.SystemClock
import java.time.*
import java.util.concurrent.atomic.AtomicBoolean

class LX200ProtocolHandler(private val server: LX200ProtocolServer) : ChannelInboundHandlerAdapter() {

    private val started = AtomicBoolean()

    @Volatile private var rightAscension = 0.0
    @Volatile private var declination = 0.0
    @Volatile private var date = LocalDate.now(SystemClock)
    @Volatile private var time = LocalTime.now(SystemClock)
    @Volatile private var offset = SystemClock.zone.rules.getOffset(Instant.now())

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        LOG.i("client connected. address={}", ctx.channel().remoteAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        LOG.i("client disconnected. address={}", ctx.channel().remoteAddress())
    }

    private fun ChannelHandlerContext.updateRA(text: String) {
        rightAscension = text.hours
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    private fun ChannelHandlerContext.updateDEC(text: String) {
        declination = text.deg
        writeAndFlush(LX200ProtocolMessage.Ok)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val input = msg as ByteBuf

        if (input.readableBytes() < 2) return

        val command = input.toString(Charsets.US_ASCII)

        LOG.d("command received: {}", command)

        if (started.get()) {
            when (command) {
                "#\u0006" -> ctx.writeAndFlush(LX200ProtocolMessage.Ack)
                "#:GR#" -> ctx.writeAndFlush(LX200ProtocolMessage.RAPosition(server.rightAscension))
                "#:GD#" -> ctx.writeAndFlush(LX200ProtocolMessage.DECPosition(server.declination))
                "#:Gg#" -> ctx.writeAndFlush(LX200ProtocolMessage.Longitude(server.longitude))
                "#:Gt#" -> ctx.writeAndFlush(LX200ProtocolMessage.Latitude(server.latitude))
                "#:GC#" -> ctx.writeAndFlush(LX200ProtocolMessage.Date(LocalDate.now(SystemClock)))
                "#:GL#" -> ctx.writeAndFlush(LX200ProtocolMessage.Time(LocalTime.now(SystemClock)))
                "#:GG#" -> ctx.writeAndFlush(LX200ProtocolMessage.ZoneOffset(SystemClock.zone.rules.getOffset(Instant.now()).totalSeconds / 3600.0))
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
                            val longitude = -command.substring(4).deg
                            server.coordinates(longitude, server.latitude)
                        }
                        command.startsWith("#:St") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            val latitude = command.substring(4).deg
                            server.coordinates(server.longitude, latitude)
                        }
                        command.startsWith("#:SL") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            time = LocalTime.parse(command.substring(4, command.length - 1), LX200ProtocolEncoder.CALENDAR_TIME_FORMAT)
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(SystemClock.zone.rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:SC") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Text("1Updating planetary data       #                              #"))
                            date = LocalDate.parse(command.substring(4, command.length - 1), LX200ProtocolEncoder.CALENDAR_DATE_FORMAT)
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(SystemClock.zone.rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:SG") -> {
                            ctx.writeAndFlush(LX200ProtocolMessage.Ok)
                            val offsetInHours = -command.substring(4, command.length - 1).toDouble()
                            offset = ZoneOffset.ofTotalSeconds((offsetInHours * 3600.0).toInt())
                            val localTime = OffsetDateTime.of(date, time, offset)
                            val utcTime = localTime.minusSeconds(SystemClock.zone.rules.getOffset(Instant.now()).totalSeconds.toLong())
                            server.time(utcTime)
                        }
                        command.startsWith("#:Sr") -> ctx.updateRA(command.substring(4))
                        command.startsWith("#:Sd") -> ctx.updateDEC(command.substring(4))
                        else -> LOG.dw("received unknown command. command={}", command)
                    }
                }
            }
        } else {
            if (command == "#\u0006") {
                ctx.writeAndFlush(LX200ProtocolMessage.Ack)
                started.set(true)
                LOG.di("LX200 protocol handling started")
            }
        }

        input.release()
    }

    companion object {

        private val LOG = loggerFor<LX200ProtocolHandler>()
    }
}
