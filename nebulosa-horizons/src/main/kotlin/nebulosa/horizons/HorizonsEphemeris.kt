package nebulosa.horizons

import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Stream
import kotlin.math.abs

class HorizonsEphemeris private constructor(private val ephemeris: MutableMap<LocalDateTime, HorizonsElement>) :
    Map<LocalDateTime, HorizonsElement> by ephemeris, ClosedRange<LocalDateTime> {

    override val start
        get() = ephemeris.keys.first()

    override val endInclusive
        get() = ephemeris.keys.last()

    constructor() : this(TreeMap<LocalDateTime, HorizonsElement>())

    override fun isEmpty() = ephemeris.isEmpty()

    override operator fun get(key: LocalDateTime): HorizonsElement? {
        val newKey = key.withSecond(0).withNano(0)
        val element = ephemeris[newKey]
        if (element != null) return element
        val interval = endInclusive.utcSeconds - start.utcSeconds
        val foundKey = ephemeris.keys.firstOrNull { it >= newKey } ?: return null
        if (abs(foundKey.utcSeconds - newKey.utcSeconds) <= interval) return ephemeris[foundKey]
        return null
    }

    override fun toString() = "HorizonsEphemeris($ephemeris)"

    companion object {

        @JvmStatic private val WHITESPACE_REGEX = Regex("\\s+")
        @JvmStatic private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.ENGLISH)
        @JvmStatic private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

        private inline val LocalDateTime.utcSeconds
            get() = toEpochSecond(ZoneOffset.UTC)

        @JvmStatic
        internal fun parse(stream: InputStream) = parse(stream.bufferedReader().lines())

        @JvmStatic
        fun of(elements: Map<LocalDateTime, HorizonsElement>): HorizonsEphemeris {
            val ephemeris = HorizonsEphemeris()
            ephemeris.ephemeris.putAll(elements)
            return ephemeris
        }

        @JvmStatic
        internal fun parse(lines: Stream<String?>): HorizonsEphemeris {
            val ephemeris = HorizonsEphemeris()

            var start = false
            var first = false

            val headerLine = arrayOfNulls<String>(4)
            val quantities = arrayListOf<HorizonsQuantity?>()

            // TODO: Handle errors.

            for (line in lines) {
                val trimmedLine = line?.trim() ?: break

                if (trimmedLine.isEmpty()) continue

                headerLine[0] = headerLine[1]
                headerLine[1] = headerLine[2]
                headerLine[2] = headerLine[3]
                headerLine[3] = trimmedLine

                if (!start) {
                    start = trimmedLine.startsWith("\$\$SOE")
                    continue
                }

                if (trimmedLine.startsWith("\$\$EOE")) break

                if (!first) {
                    first = true

                    headerLine[0]!!
                        .splitToSequence(',')
                        .map(String::trim)
                        .forEach { HorizonsQuantity.parse(it).also(quantities::add) }
                }

                val parts = trimmedLine.split(',').map(String::trim)
                val dateTimeParts = parts[0].split(WHITESPACE_REGEX)

                val date = LocalDate.parse(dateTimeParts[0], DATE_FORMAT)
                val time = LocalTime.parse(dateTimeParts[1], TIME_FORMAT)
                val dateTime = LocalDateTime.of(date, time)

                val element = HorizonsElement()

                for (i in 1 until parts.size) {
                    val quantity = quantities[i] ?: continue
                    element[quantity] = parts[i]
                }

                ephemeris.ephemeris[dateTime] = element
            }

            return ephemeris
        }
    }
}
