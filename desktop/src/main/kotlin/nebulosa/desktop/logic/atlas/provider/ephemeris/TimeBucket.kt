package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.constants.DAYSEC
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class TimeBucket private constructor(private val bucket: MutableList<Pair<UTC, LocalDateTime>>) : List<Pair<UTC, LocalDateTime>> by bucket {

    constructor() : this(ArrayList(1441))

    @Synchronized
    fun compute(force: Boolean, startTime: LocalDateTime): Boolean {
        return if (force || bucket.isEmpty() || startTime != bucket[0].second) {
            bucket.clear()

            val step = 1.0 / STEP_SIZE
            val whole = TimeYMDHMS(startTime).value

            LOG.info("computing time. startTime={}, step={}", startTime, step)

            for (i in 0..STEP_SIZE) {
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

        private const val STEP_SIZE = 24 * 60

        @JvmStatic private val LOG = LoggerFactory.getLogger(TimeBucket::class.java)
    }
}
