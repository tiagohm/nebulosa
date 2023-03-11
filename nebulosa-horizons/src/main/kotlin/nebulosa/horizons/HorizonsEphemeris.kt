package nebulosa.horizons

import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Stream

class HorizonsEphemeris(val elements: Array<HorizonsElement>) : ClosedRange<LocalDateTime> {

    val times = Array(elements.size) { elements[it].time }

    override val start
        get() = elements.first().time

    override val endInclusive
        get() = elements.last().time

    override fun isEmpty() = elements.isEmpty()

    operator fun get(key: LocalDateTime): HorizonsElement? {
        val minutes = key.utcMinutes
        return elements.find { it.utcMinutes >= minutes }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HorizonsEphemeris) return false

        if (!elements.contentEquals(other.elements)) return false

        return true
    }

    override fun hashCode() = elements.contentHashCode()

    override fun toString() = "HorizonsEphemeris(${elements.contentToString()})"

    companion object {

        @JvmStatic private val WHITESPACE_REGEX = Regex("\\s+")
        @JvmStatic private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.ENGLISH)
        @JvmStatic private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

        private inline val LocalDateTime.utcSeconds
            get() = toEpochSecond(ZoneOffset.UTC)

        private inline val LocalDateTime.utcMinutes
            get() = utcSeconds / 60L

        @JvmStatic
        internal fun parse(stream: InputStream) = parse(stream.bufferedReader().lines())

        @JvmStatic
        internal fun parse(lines: Stream<String?>): HorizonsEphemeris {
            var start = false
            var first = false

            val headerLine = arrayOfNulls<String>(4)
            val quantities = arrayListOf<HorizonsQuantity?>()
            val elements = ArrayList<HorizonsElement>(1441)

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

                val element = HorizonsElement(dateTime)

                var i = 1

                while (i < parts.size) {
                    val quantity = quantities[i]
                    if (quantity != null)
                        element[quantity] = if (quantity.numberOfColumns == 1) parts[i]
                        else (0 until quantity.numberOfColumns).joinToString(",") { parts[i + it] }
                    i += quantity?.numberOfColumns ?: 1
                }

                elements.add(element)
            }

            return HorizonsEphemeris(elements.toTypedArray())
        }
    }
}
