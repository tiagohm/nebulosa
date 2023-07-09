@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.helpers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

inline fun LocalDate?.orNow(): LocalDate = this ?: LocalDate.now()

inline fun LocalTime?.orNow(): LocalTime = this ?: LocalTime.now()

inline operator fun LocalDate?.plus(time: LocalTime?): LocalDateTime = LocalDateTime.of(orNow(), time.orNow())

inline fun LocalDateTime.noSeconds(): LocalDateTime = withSecond(0).withNano(0)
