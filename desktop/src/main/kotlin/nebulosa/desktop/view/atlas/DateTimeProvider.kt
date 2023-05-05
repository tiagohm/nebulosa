package nebulosa.desktop.view.atlas

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

interface DateTimeProvider {

    val date: LocalDate

    val time: LocalTime

    val timeOffset: ZoneOffset

    val offsetInSeconds
        get() = timeOffset.totalSeconds

    fun resetTime()

    open class Now(override val timeOffset: ZoneOffset) : DateTimeProvider {

        override val date: LocalDate
            get() = LocalDate.now(timeOffset)

        override val time: LocalTime
            get() = LocalTime.now(timeOffset).withSecond(0).withNano(0)

        override fun resetTime() = Unit
    }

    object Utc : Now(ZoneOffset.UTC)
}
