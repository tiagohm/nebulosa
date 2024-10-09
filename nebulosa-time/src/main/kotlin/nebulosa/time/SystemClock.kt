package nebulosa.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object SystemClock : Clock() {

    private val clock = systemDefaultZone()

    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = clock.withZone(zone)

    override fun instant(): Instant = clock.instant()
}
