package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.log.loggerFor
import nebulosa.nova.position.GeographicPosition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

abstract class CachedEphemerisProvider<T : Any> : EphemerisProvider<T> {

    @JvmField protected val ephemeris = HashMap<Pair<T, GeographicPosition>, MutableMap<LocalDateTime, HorizonsElement>>()

    protected abstract fun compute(
        target: T,
        position: GeographicPosition,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<HorizonsElement>

    final override fun compute(
        target: T,
        position: GeographicPosition,
        dateTime: LocalDateTime,
        timeOffset: ZoneOffset,
    ): List<HorizonsElement> {
        val offsetInSeconds = timeOffset.totalSeconds.toLong()
        val date = dateTime.toLocalDate()

        val startTime = if (dateTime.hour >= 12) LocalDateTime.of(date, NOON).minusSeconds(offsetInSeconds).withSecond(0).withNano(0)
        else LocalDateTime.of(date, NOON).minusDays(1L).minusSeconds(offsetInSeconds).withSecond(0).withNano(0)
        val endTime = startTime.plusDays(1L)

        LOG.info("computing ephemeris for {} from {} UTC to {} UTC. dateTime={}", target, startTime, endTime, dateTime)

        val key = target to position

        return synchronized(this) {
            if (key in ephemeris) {
                val elements = ephemeris[key]!!
                val res = ArrayList<HorizonsElement>(1441)

                for (minute in 0L..1440L) {
                    val time = startTime.plusMinutes(minute)

                    if (time !in elements) {
                        res.addAll(computeAndSave(key, time, endTime))
                        break
                    } else {
                        res.add(elements[time]!!)
                    }
                }

                res
            } else {
                computeAndSave(key, startTime, endTime)
            }
        }
    }

    private fun computeAndSave(
        key: Pair<T, GeographicPosition>,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<HorizonsElement> {
        LOG.info("retrieving ephemeris. target={}, position={}, startTime={}, endTime={}", key.first, key.second, startTime, endTime)

        val elements = compute(key.first, key.second, startTime, endTime)
        val cachedElements = ephemeris.getOrPut(key) { HashMap(1441) }
        elements.forEach { cachedElements[it.dateTime] = it }
        return elements
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CachedEphemerisProvider<*>>()
        @JvmStatic private val NOON = LocalTime.of(12, 0, 0, 0)
    }
}
