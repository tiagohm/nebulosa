package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.log.debug
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

        LOG.debug { "computing ephemeris for $target from $startTime UTC to $endTime UTC" }

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
        LOG.debug { "retrieving ephemeris. target=${key.first}, position=${key.second}, startTime=$startTime, endTime=$endTime" }
        val elements = compute(key.first, key.second, startTime, endTime)
        LOG.debug { "retrieved ephemeris. size=${elements.size}, target=${key.first}, position=${key.second}, startTime=$startTime, endTime=$endTime" }
        val cachedElements = ephemeris.getOrPut(key) { HashMap(ChronoUnit.MINUTES.between(startTime, endTime).toInt() + 1) }
        elements.forEach { cachedElements[it.dateTime] = it }
        return elements
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CachedEphemerisProvider<*>>()
        @JvmStatic private val NOON = LocalTime.of(12, 0, 0, 0)
    }
}
