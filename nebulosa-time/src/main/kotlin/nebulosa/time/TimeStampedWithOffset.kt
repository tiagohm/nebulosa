package nebulosa.time

import java.time.LocalDateTime

interface TimeStampedWithOffset : TimeZonedInSeconds {

    val dateTime: LocalDateTime
}
