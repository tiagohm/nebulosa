package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.desktop.view.atlas.DateTimeProvider
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.log.loggerFor
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.UTC
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
        dateTimeProvider: DateTimeProvider,
        force: Boolean,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris? {
        val date = dateTimeProvider.date
        val timeOffset = dateTimeProvider.timeOffset
        val offsetInSeconds = timeOffset.totalSeconds.toLong()
        val now = OffsetDateTime.of(date, LocalTime.now(timeOffset), timeOffset)

        val isToday = LocalDate.now(timeOffset).compareTo(date) == 0
        val startTime = if (!isToday || now.hour >= 12) LocalDateTime.of(date, NOON).minusSeconds(offsetInSeconds)
        else LocalDateTime.of(date, NOON).minusDays(1L).minusSeconds(offsetInSeconds)

        if (position !in ephemerisCache) ephemerisCache[position] = hashMapOf()

        return if (timeBucket.compute(force, startTime)
            || target !in ephemerisCache[position]!!
            || startTime != ephemerisCache[position]!![target]!!.start
        ) {
            val endTime = timeBucket.last().second

            LOG.info("retrieving ephemeris from JPL Horizons. target={}, startTime={}, endTime={}, force={}", target, startTime, endTime, force)

            compute(target, position, timeBucket, *quantities)
                ?.also { ephemerisCache[position]!![target] = it }
        } else {
            ephemerisCache[position]!![target]
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AbstractEphemerisProvider<*>>()
        @JvmStatic private val NOON = LocalTime.of(0, 0, 0, 0)
    }
}
