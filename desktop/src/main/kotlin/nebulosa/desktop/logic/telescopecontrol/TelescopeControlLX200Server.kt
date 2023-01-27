package nebulosa.desktop.logic.telescopecontrol

import nebulosa.indi.device.mounts.Mount
import nebulosa.math.Angle
import org.slf4j.LoggerFactory
import java.net.Socket
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Meade Telescope Serial Command Protocol.
 *
 * @see <a href="http://www.company7.com/library/meade/LX200CommandSet.pdf">Meade Telescope Serial Command Protocol</a>
 */
class TelescopeControlLX200Server(
    mount: Mount,
    host: String = "0.0.0.0",
    port: Int = 10001,
) : TelescopeControlTCPServer(mount, host, port) {

    override fun acceptSocket(socket: Socket): Client = TelescopeClient(this, socket)

    private class TelescopeClient(
        val server: TelescopeControlLX200Server,
        socket: Socket,
    ) : Client(socket) {

        @Volatile private var started = false
        @Volatile private var rightAscension = Angle.ZERO
        @Volatile private var declination = Angle.ZERO
        @Volatile private var updateRADEC = false

        override fun processMessage(): Boolean {
            val readCount = input.read(buffer, 32L)

            if (readCount < 0) return false

            if (started) processCommand()
            else processACK()

            return true
        }

        fun sendRAPosition(ra: Angle) {
            output.writeString(Angle.formatHMS(ra, "+%02d:%02d:%02.0f#"), Charsets.US_ASCII)
            output.flush()
        }

        fun sendDECPosition(dec: Angle) {
            output.writeString(Angle.formatDMS(dec, "%s%02d*%02d:%02.0f#"), Charsets.US_ASCII)
            output.flush()
        }

        fun sendLongitude(longitude: Angle) {
            // East is negative.
            output.writeString(Angle.formatDMS(-longitude, "%s%03d*%02d#"), Charsets.US_ASCII)
            output.flush()
        }

        fun sendLatitude(latitude: Angle) {
            output.writeString(Angle.formatDMS(latitude, "%s%02d*%02d#"), Charsets.US_ASCII)
            output.flush()
        }

        fun sendCalendarDate() {
            val date = LocalDate.now().format(CALENDAR_DATE_FORMAT)
            output.writeString("$date#", Charsets.US_ASCII)
            output.flush()
        }

        fun sendLocalTime() {
            val time = LocalTime.now().format(CALENDAR_TIME_FORMAT)
            output.writeString("$time#", Charsets.US_ASCII)
            output.flush()
        }

        fun sendTimeOffset() {
            val offset = ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 3600.0
            val sign = if (offset >= 0) "-" else "+"
            output.writeString("%s%04.01f#".format(sign, abs(offset)), Charsets.US_ASCII)
            output.flush()
        }

        private fun sendSlewingStatus(slewing: Boolean) {
            output.writeString(if (slewing) "|#" else "#", Charsets.US_ASCII)
            output.flush()
        }

        private fun updateRA(text: String) {
            val parts = text.split(":")
            rightAscension = Angle.hms(parts[0].toInt(), parts[1].toInt(), parts[2].toDouble())
            sendOk()
            updateRADEC = true
        }

        private fun updateDEC(text: String) {
            val parts = text.split(":")
            declination = Angle.dms(parts[0].toInt(), parts[1].toInt(), parts[2].toDouble())
            sendOk()
            updateRADEC = true
        }

        private fun sync() {
            if (updateRADEC) {
                server.mount.syncJ2000(rightAscension, declination)
                updateRADEC = false
            }
        }

        private fun move() {
            if (updateRADEC) {
                server.mount.goToJ2000(rightAscension, declination)
                updateRADEC = false
            }
        }

        private fun abort() {
            server.mount.abortMotion()
        }

        private fun sendOk() {
            output.writeByte(49)
            output.flush()
        }

        private fun processACK() {
            while (!buffer.exhausted()) {
                val byte = buffer.readByte()

                if (byte == 6.toByte()) {
                    output.writeByte(71) // G
                    output.flush()
                    started = true
                }
            }
        }

        private fun processCommand() {
            var commandStarted = false
            val command = ByteArray(12)
            var commandIdx = 0

            while (!buffer.exhausted()) {
                val byte = buffer.readByte()

                // #
                if (!commandStarted && byte == 35.toByte()) {
                    commandStarted = true
                } else if (commandStarted && byte == 35.toByte()) {
                    break
                } else {
                    command[commandIdx++] = byte
                }
            }

            when (val c = String(command, 0, commandIdx)) {
                "\u0006" -> {
                    output.writeByte(71) // G
                    output.flush()
                }
                ":GR" -> sendRAPosition(server.mount.rightAscensionJ2000)
                ":GD" -> sendDECPosition(server.mount.declinationJ2000)
                ":Gg" -> sendLongitude(server.mount.longitude)
                ":Gt" -> sendLatitude(server.mount.latitude)
                ":GC" -> sendCalendarDate()
                ":GL" -> sendLocalTime()
                ":GG" -> sendTimeOffset()
                // ":GW" -> sendOk()
                ":CM" -> sync()
                ":MS" -> move()
                // ":RC", ":RG", ":RM", ":RS" -> return // movement rate
                // ":Me", ":Mn", ":Ms", ":Mw" -> return // move
                // ":Qe", ":Qn", ":Qs", ":Qw" -> return // abort move
                ":Q" -> abort()
                ":U" -> return
                ":D" -> sendSlewingStatus(server.mount.isSlewing)
                else -> {
                    when {
                        c.startsWith(":Sg") -> sendOk() // Longitude
                        c.startsWith(":St") -> sendOk() // Latitude
                        c.startsWith(":SL") -> sendOk() // Local Time
                        c.startsWith(":SC") -> sendOk() // Calendar Date
                        c.startsWith(":SG") -> sendOk() // Time Offset
                        c.startsWith(":Sr") -> return updateRA(c.substring(3))
                        c.startsWith(":Sd") -> return updateDEC(c.substring(3))
                        else -> LOG.warn("received unknown command: $c")
                    }
                }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TelescopeControlLX200Server::class.java)
        @JvmStatic private val CALENDAR_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy")
        @JvmStatic private val CALENDAR_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
