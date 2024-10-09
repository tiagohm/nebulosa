@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import nebulosa.time.SystemClock
import java.time.LocalDate
import java.time.LocalTime

inline fun Context.localDate(): LocalDate = localDateOrNull() ?: LocalDate.now(SystemClock)
inline fun Context.localDateOrNull() = queryParam("date")?.let(LocalDate::parse)
inline fun Context.localTime(): LocalTime = localTimeOrNull() ?: LocalTime.now(SystemClock)
inline fun Context.localTimeOrNull() = queryParam("time")?.let(LocalTime::parse)

