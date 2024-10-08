@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*
import io.javalin.validation.Check
import io.javalin.validation.Validator

typealias DoubleRange = ClosedFloatingPointRange<Double>

inline fun Context.pathParamAsDouble(key: String) = pathParamAsClass<Double>(key)
inline fun Context.queryParamAsDouble(key: String) = queryParamAsClass<Double>(key)
inline fun Context.formParamAsDouble(key: String) = formParamAsClass<Double>(key)
inline fun Context.headerAsDouble(key: String) = headerAsClass<Double>(key)

inline fun Validator<Double>.min(min: Double) = check(DoubleMinCheck(min), "must be greater or equal to $min")
inline fun Validator<Double>.max(max: Double) = check(DoubleMaxCheck(max), "must be less or equal to $max")
inline fun Validator<Double>.range(range: DoubleRange) = check(DoubleRangeCheck(range), "must be between ${range.start} and ${range.endInclusive}")
inline fun Validator<Double>.range(min: Double, max: Double) = range(min..max)
inline fun Validator<Double>.positive() = check(DoublePositiveCheck, "must be greater than 0")
inline fun Validator<Double>.positiveOrZero() = check(DoublePositiveOrZeroCheck, "must be greater or equal to 0")

@PublishedApi
internal data class DoubleMinCheck(private val min: Double) : Check<Double> {

    override fun invoke(p: Double) = p >= min
}

@PublishedApi
internal data class DoubleMaxCheck(private val max: Double) : Check<Double> {

    override fun invoke(p: Double) = p <= max
}

@PublishedApi
internal data class DoubleRangeCheck(private val range: DoubleRange) : Check<Double> {

    override fun invoke(p: Double) = p in range
}

@PublishedApi
internal data object DoublePositiveCheck : Check<Double> {

    override fun invoke(p: Double) = p > 0
}

@PublishedApi
internal data object DoublePositiveOrZeroCheck : Check<Double> {

    override fun invoke(p: Double) = p >= 0
}
