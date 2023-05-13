package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.constants.DAYSEC
import nebulosa.log.loggerFor
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import java.time.LocalDateTime

class TimeBucket private constructor(
    private val bucket: MutableList<Pair<UTC, LocalDateTime>>,
    private val stepCount: Int,
) : List<Pair<UTC, LocalDateTime>> by bucket {

    constructor(stepCount: Int) : this(ArrayList(stepCount + 1), stepCount)

    @Synchronized
    fun compute(force: Boolean, startTime: LocalDateTime): Boolean {
        return if (force || bucket.isEmpty() || startTime != bucket[0].second) {
            bucket.clear()

            val step = 1.0 / stepCount
            val whole = TimeYMDHMS(startTime).value

            LOG.info("computing time. startTime={}, step={}", startTime, step)

            for (i in 0..stepCount) {
                val fraction = i * step
                val utc = UTC(whole, fraction)
                bucket.add(utc to startTime.plusSeconds((fraction * DAYSEC).toLong()))
            }

            true
        } else {
            false
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TimeBucket>()
    }
}
