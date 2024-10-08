@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*
import io.javalin.validation.Check
import io.javalin.validation.Validator

inline fun Context.pathParamAsLong(key: String) = pathParamAsClass<Long>(key)
inline fun Context.queryParamAsLong(key: String) = queryParamAsClass<Long>(key)
inline fun Context.formParamAsLong(key: String) = formParamAsClass<Long>(key)
inline fun Context.headerAsLong(key: String) = headerAsClass<Long>(key)

inline fun Validator<Long>.min(min: Long) = check(LongMinCheck(min), "should be greater or equal to $min")
inline fun Validator<Long>.max(max: Long) = check(LongMaxCheck(max), "should be less or equal to $max")
inline fun Validator<Long>.range(range: LongRange) = check(LongRangeCheck(range), "should be between ${range.first} and ${range.last}")
inline fun Validator<Long>.range(min: Long, max: Long) = range(min..max)
inline fun Validator<Long>.positive() = check(LongPositiveCheck, "should be greater than 0")
inline fun Validator<Long>.positiveOrZero() = check(LongPositiveOrZeroCheck, "should be greater or equal to 0")

@PublishedApi
internal data class LongMinCheck(private val min: Long) : Check<Long> {

    override fun invoke(p: Long) = p >= min
}

@PublishedApi
internal data class LongMaxCheck(private val max: Long) : Check<Long> {

    override fun invoke(p: Long) = p <= max
}

@PublishedApi
internal data class LongRangeCheck(private val range: LongRange) : Check<Long> {

    override fun invoke(p: Long) = p in range
}

@PublishedApi
internal data object LongPositiveCheck : Check<Long> {

    override fun invoke(p: Long) = p > 0
}

@PublishedApi
internal data object LongPositiveOrZeroCheck : Check<Long> {

    override fun invoke(p: Long) = p >= 0
}
