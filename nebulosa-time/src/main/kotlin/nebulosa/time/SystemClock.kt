package nebulosa.time

import java.time.Clock
import java.time.ZoneId

object SystemClock : Clock() {

    private val clock = systemDefaultZone()

    override fun getZone() = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId) = clock.withZone(zone)

    override fun instant() = clock.instant()
}
