package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.nova.position.GeographicPosition
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

abstract class CachedEphemerisProvider<T : Any> : EphemerisProvider<T> {

    @JvmField protected val ephemeris = HashMap<Pair<T, GeographicPosition>, MutableMap<LocalDateTime, HorizonsElement>>()

    protected abstract fun compute(
        target: T, position: GeographicPosition,
        startTime: LocalDateTime, endTime: LocalDateTime,
    ): List<HorizonsElement>

    final override fun compute(
        target: T, position: GeographicPosition,
        dateTime: LocalDateTime, offsetInSeconds: Long, fully: Boolean,
    ): List<HorizonsElement> {
        val date = dateTime.toLocalDate()

        val startTime = if (fully) if (dateTime.hour >= 12) LocalDateTime.of(date, NOON).minusSeconds(offsetInSeconds).withSecond(0).withNano(0)
        else LocalDateTime.of(date, NOON).minusDays(1L).minusSeconds(offsetInSeconds).withSecond(0).withNano(0)
        else dateTime.minusSeconds(offsetInSeconds).withSecond(0).withNano(0)

        val endTime = if (fully) startTime.plusDays(1L) else startTime.plusMinutes(1L)

        LOG.d { debug("computing ephemeris for {} from {} UTC to {} UTC", target, startTime, endTime) }

        val key = target to position

        return if (key in ephemeris) {
            var time = startTime
            val elements = ephemeris[key]!!
            val numberOfElements = if (fully) 1441 else 1
            val output = ArrayList<HorizonsElement>(numberOfElements)

            while (time <= endTime) {
                if (time !in elements) {
                    output.addAll(computeAndSave(key, time, endTime))
                    break
                } else {
                    output.add(elements[time]!!)
                }

                time = time.plusMinutes(1)
            }

            output
        } else {
            computeAndSave(key, startTime, endTime)
        }
    }

    private fun computeAndSave(
        key: Pair<T, GeographicPosition>,
        startTime: LocalDateTime, endTime: LocalDateTime,
    ): List<HorizonsElement> {
        LOG.d { debug("retrieving ephemeris. target={}, position={}, startTime={}, endTime={}", key.first, key.second, startTime, endTime) }
        val elements = compute(key.first, key.second, startTime, endTime)
        LOG.d { debug("retrieved ephemeris. size={}, target={}, position={}, startTime={}, endTime={}", elements.size, key.first, key.second, startTime, endTime) }
        val cachedElements = ephemeris.getOrPut(key) { HashMap(ChronoUnit.MINUTES.between(startTime, endTime).toInt() + 1) }
        elements.forEach { cachedElements[it.dateTime] = it }
        return elements
    }

    companion object {

        private val LOG = loggerFor<CachedEphemerisProvider<*>>()
        private val NOON = LocalTime.of(12, 0, 0, 0)
    }
}
