@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*
import io.javalin.validation.Check
import io.javalin.validation.Validator

inline fun Context.pathParamAsString(key: String) = pathParamAsClass<String>(key)
inline fun Context.queryParamAsString(key: String) = queryParamAsClass<String>(key)
inline fun Context.formParamAsString(key: String) = formParamAsClass<String>(key)
inline fun Context.headerAsString(key: String) = headerAsClass<String>(key)

inline fun Validator<String>.minLength(min: Int) = check(MinLengthCheck(min), "length should be greater or equal to $min")
inline fun Validator<String>.maxLength(max: Int) = check(MaxLengthCheck(max), "length should be less or equal to $max")
inline fun Validator<String>.regex(regex: Regex) = check(RegexCheck(regex), "regex does not match")

@PublishedApi
internal data class MinLengthCheck(private val min: Int) : Check<CharSequence?> {

    override fun invoke(p: CharSequence?) = p == null || p.length >= min
}

@PublishedApi
internal data class MaxLengthCheck(private val max: Int) : Check<CharSequence?> {

    override fun invoke(p: CharSequence?) = p == null || p.length <= max
}

@PublishedApi
internal data class RegexCheck(private val regex: Regex) : Check<CharSequence?> {

    override fun invoke(p: CharSequence?) = p == null || regex.matches(p)
}
