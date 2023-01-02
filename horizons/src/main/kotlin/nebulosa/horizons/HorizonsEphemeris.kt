package nebulosa.horizons

import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class HorizonsEphemeris(val bodyName: String) : TreeMap<LocalDateTime, HorizonsElement>(), ClosedRange<LocalDateTime> {

    override val start: LocalDateTime get() = firstKey() ?: LocalDateTime.now()

    override val endInclusive: LocalDateTime get() = lastKey() ?: start

    override fun isEmpty() = size == 0

    fun now(): HorizonsElement? {
        val dateTime = LocalDateTime.now(ZoneOffset.UTC)

        return if (dateTime in this) {
            this[dateTime.withSecond(0).withNano(0)]!!
        } else {
            null
        }
    }

    companion object {

        @JvmStatic private val TARGET_BODY_NAME_REGEX = Regex("^Target body name: (.*) \\(\\d+\\).*")
        @JvmStatic private val EPHEMERIS_LINE_REGEX = Regex("^\\d{4}-\\w{3}-\\d{2} \\d{2}:\\d{2}.*")
        @JvmStatic private val WHITESPACE_REGEX = Regex("\\s+")
        @JvmStatic private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.ENGLISH)
        @JvmStatic private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.ENGLISH)

        @JvmStatic
        internal fun parse(
            stream: InputStream,
            vararg quantities: HorizonsQuantity,
        ): HorizonsEphemeris {
            lateinit var ephemeris: HorizonsEphemeris

            val lines = stream
                .bufferedReader()
                .lines()

            val size = quantities.sumOf { it.size } + 3

            var start = false
            var end = false

            // TODO: Handle errors.

            for (line in lines) {
                val trimmedLine = line.trim()

                if (trimmedLine.isEmpty()) continue

                if (!start) start = trimmedLine.startsWith("\$\$SOE")
                if (!end) end = trimmedLine.startsWith("\$\$EOE")

                if (start) {
                    if (EPHEMERIS_LINE_REGEX.matches(trimmedLine)) {
                        val parts = trimmedLine.split(WHITESPACE_REGEX)

                        if (parts.size != size) continue

                        val date = LocalDate.parse(parts[0], DATE_FORMAT)
                        val time = LocalTime.parse(parts[1], TIME_FORMAT)
                        val dateTime = LocalDateTime.of(date, time)
                        val element = HorizonsElement()

                        var pos = 3

                        for (quantity in quantities) {
                            val values = Array(quantity.size) { parts[pos++] }
                            element[quantity] = values.joinToString(" ")
                        }

                        ephemeris[dateTime] = element
                    }
                } else if (end) {
                    break
                } else {
                    val m = TARGET_BODY_NAME_REGEX.matchEntire(trimmedLine) ?: continue
                    val targetBodyName = m.groupValues[1]
                    ephemeris = HorizonsEphemeris(targetBodyName)
                }
            }

            return ephemeris
        }
    }
}
