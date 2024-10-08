@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*
import io.javalin.validation.Check
import io.javalin.validation.Validator

inline fun Context.pathParamAsInt(key: String) = pathParamAsClass<Int>(key)
inline fun Context.queryParamAsInt(key: String) = queryParamAsClass<Int>(key)
inline fun Context.formParamAsInt(key: String) = formParamAsClass<Int>(key)
inline fun Context.headerAsInt(key: String) = headerAsClass<Int>(key)

inline fun Validator<Int>.min(min: Int) = check(IntMinCheck(min), "should be greater or equal to $min")
inline fun Validator<Int>.max(max: Int) = check(IntMaxCheck(max), "should be less or equal to $max")
inline fun Validator<Int>.range(range: IntRange) = check(IntRangeCheck(range), "should be between ${range.first} and ${range.last}")
inline fun Validator<Int>.range(min: Int, max: Int) = range(min..max)
inline fun Validator<Int>.positive() = check(IntPositiveCheck, "should be greater than 0")
inline fun Validator<Int>.positiveOrZero() = check(IntPositiveOrZeroCheck, "should be greater or equal to 0")

@PublishedApi
internal data class IntMinCheck(private val min: Int) : Check<Int> {

    override fun invoke(p: Int) = p >= min
}

@PublishedApi
internal data class IntMaxCheck(private val max: Int) : Check<Int> {

    override fun invoke(p: Int) = p <= max
}

@PublishedApi
internal data class IntRangeCheck(private val range: IntRange) : Check<Int> {

    override fun invoke(p: Int) = p in range
}

@PublishedApi
internal data object IntPositiveCheck : Check<Int> {

    override fun invoke(p: Int) = p > 0
}

@PublishedApi
internal data object IntPositiveOrZeroCheck : Check<Int> {

    override fun invoke(p: Int) = p >= 0
}
