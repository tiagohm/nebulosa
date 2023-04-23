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

    private val ephemerisCache = hashMapOf<GeographicPosition, MutableMap<T, HorizonsEphemeris>>()

    abstract fun compute(
        target: T,
        position: GeographicPosition,
        timeSpan: List<Pair<UTC, LocalDateTime>>,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris?

    private fun computeTime(force: Boolean, startTime: LocalDateTime): Boolean {
        return synchronized(TIME_BUCKET) {
            if (force || TIME_BUCKET.isEmpty() || startTime != TIME_BUCKET[0].second) {
                TIME_BUCKET.clear()

                val step = 1.0 / STEP_COUNT
                val whole = TimeYMDHMS(startTime).value

                LOG.info("computing time. startTime={}, step={}", startTime, step)

                for (i in 0..STEP_COUNT) {
                    val fraction = i * step
                    val utc = UTC(whole, fraction)
                    TIME_BUCKET.add(utc to startTime.plusSeconds((fraction * DAYSEC).toLong()))
                }

                true
            } else {
                false
            }
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

        return if (computeTime(force, startTime) || target !in ephemerisCache[position]!!) {
            val endTime = TIME_BUCKET.last().second

            LOG.info(
                "retrieving ephemeris from JPL Horizons. interval={}, target={}, startTime={}, endTime={}",
                TIME_BUCKET.size, target, startTime, endTime,
            )

            compute(target, position, TIME_BUCKET, *quantities)
                ?.also { ephemerisCache[position]!![target] = it }
        } else {
            ephemerisCache[position]!![target]
        }
    }

    companion object {

        private const val STEP_COUNT = 24 * 60

        @JvmStatic private val LOG = LoggerFactory.getLogger(AbstractEphemerisProvider::class.java)
        @JvmStatic private val TIME_BUCKET = ArrayList<Pair<UTC, LocalDateTime>>(1441)
    }
}
