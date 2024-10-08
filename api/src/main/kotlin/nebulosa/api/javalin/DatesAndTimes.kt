@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import io.javalin.http.queryParamAsClass
import io.javalin.validation.Check
import io.javalin.validation.Validator
import nebulosa.time.SystemClock
import java.time.LocalDate
import java.time.LocalTime

inline fun Context.localDate(): LocalDate = localDateOrNull() ?: LocalDate.now(SystemClock)
inline fun Context.localDateOrNull() = queryParamAsClass<LocalDate>("date").allowNullable().get()
inline fun Context.localTime(): LocalTime = localTimeOrNull() ?: LocalTime.now(SystemClock)
inline fun Context.localTimeOrNull() = queryParamAsClass<LocalTime>("time").allowNullable().get()

inline fun Validator<LocalDate>.min(min: LocalDate) = check(LocalDateMinCheck(min), "must be after the $min")
inline fun Validator<LocalDate>.max(max: LocalDate) = check(LocalDateMaxCheck(max), "must be before the $max")

@JvmName("futureDate")
inline fun Validator<LocalDate>.future() = check(LocalDateFutureCheck, "must be in the future")

inline fun Validator<LocalTime>.min(min: LocalTime) = check(LocalTimeMinCheck(min), "must be after the $min")
inline fun Validator<LocalTime>.max(max: LocalTime) = check(LocalTimeMaxCheck(max), "must be before the $max")

@JvmName("futureTime")
inline fun Validator<LocalTime>.future() = check(LocalTimeFutureCheck, "must be in the future")

@PublishedApi
internal data class LocalDateMinCheck(private val min: LocalDate) : Check<LocalDate> {

    override fun invoke(p: LocalDate) = p >= min
}

@PublishedApi
internal data class LocalDateMaxCheck(private val max: LocalDate) : Check<LocalDate> {

    override fun invoke(p: LocalDate) = p <= max
}

@PublishedApi
internal data object LocalDateFutureCheck : Check<LocalDate> {

    override fun invoke(p: LocalDate) = p > LocalDate.now(SystemClock)
}

@PublishedApi
internal data class LocalTimeMinCheck(private val min: LocalTime) : Check<LocalTime> {

    override fun invoke(p: LocalTime) = p >= min
}

@PublishedApi
internal data class LocalTimeMaxCheck(private val max: LocalTime) : Check<LocalTime> {

    override fun invoke(p: LocalTime) = p <= max
}

@PublishedApi
internal data object LocalTimeFutureCheck : Check<LocalTime> {

    override fun invoke(p: LocalTime) = p > LocalTime.now(SystemClock)
}

