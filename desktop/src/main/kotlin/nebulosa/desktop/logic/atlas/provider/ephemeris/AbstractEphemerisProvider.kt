package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.constants.DAYSEC
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime

abstract class AbstractEphemerisProvider<T> : EphemerisProvider<T> {

    private val timeCache = ArrayList<Pair<UTC, LocalDateTime>>(1441)
    private val ephemerisCache = hashMapOf<GeographicPosition, MutableMap<T, HorizonsEphemeris>>()

    abstract fun compute(
        target: T,
        position: GeographicPosition,
        timeSpan: List<Pair<UTC, LocalDateTime>>,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris?

    @Synchronized
    private fun shouldCompute(force: Boolean, startTime: LocalDateTime): Boolean {
        return if (force || timeCache.isEmpty() || startTime != timeCache[0].second) {
            timeCache.clear()

            val stepCount = 24.0 * 60.0
            val whole = TimeYMDHMS(startTime).value

            LOG.info("computing time. startTime={}", startTime)

            for (i in 0..stepCount.toInt()) {
                val fraction = i / stepCount // 0..1
                val utc = UTC(whole, fraction)
                timeCache.add(utc to startTime.plusSeconds((fraction * DAYSEC).toLong()))
            }

            true
        } else {
            false
        }
    }

    final override fun compute(
        target: T,
        position: GeographicPosition,
        force: Boolean,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris? {
        val now = OffsetDateTime.now()
        val offset = now.offset.totalSeconds.toLong()
        val startTime = if (now.hour >= 12) LocalDateTime.of(now.year, now.month, now.dayOfMonth, 12, 0, 0, 0).minusSeconds(offset)
        else LocalDateTime.of(now.year, now.month, now.dayOfMonth, 12, 0, 0, 0).minusDays(1L).minusSeconds(offset)

        if (position !in ephemerisCache) ephemerisCache[position] = hashMapOf()

        return if (shouldCompute(force, startTime) || target !in ephemerisCache[position]!!) {
            val endTime = timeCache.last().second

            LOG.info(
                "retrieving ephemeris from JPL Horizons. interval={}, target={}, startTime={}, endTime={}",
                timeCache.size, target, startTime, endTime,
            )

            compute(target, position, timeCache, *quantities)
                ?.also { ephemerisCache[position]!![target] = it }
        } else {
            ephemerisCache[position]!![target]
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AbstractEphemerisProvider::class.java)
    }
}
