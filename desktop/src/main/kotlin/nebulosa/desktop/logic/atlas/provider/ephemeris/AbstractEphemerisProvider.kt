package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.UTC
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime

abstract class AbstractEphemerisProvider<T> : EphemerisProvider<T> {

    private val ephemerisCache = hashMapOf<GeographicPosition, MutableMap<T, HorizonsEphemeris>>()

    abstract val timeBucket: TimeBucket

    abstract fun compute(
        target: T,
        position: GeographicPosition,
        timeSpan: List<Pair<UTC, LocalDateTime>>,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris?

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

        return if (timeBucket.compute(force, startTime) || target !in ephemerisCache[position]!!) {
            val endTime = timeBucket.last().second

            LOG.info("retrieving ephemeris from JPL Horizons. target={}, startTime={}, endTime={}", target, startTime, endTime)

            compute(target, position, timeBucket, *quantities)
                ?.also { ephemerisCache[position]!![target] = it }
        } else {
            ephemerisCache[position]!![target]
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AbstractEphemerisProvider::class.java)
    }
}
