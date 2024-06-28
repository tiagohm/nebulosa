package nebulosa.time

import java.time.OffsetDateTime

interface TimeStampedWithOffset : TimeZonedInSeconds {

    val dateTime: OffsetDateTime

    override val offsetInSeconds
        get() = dateTime.offset.totalSeconds
}
