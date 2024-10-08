@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*
import io.javalin.validation.Check
import io.javalin.validation.Validator

inline fun Context.pathParamAsString(key: String) = pathParamAsClass<String>(key)
inline fun Context.queryParamAsString(key: String) = queryParamAsClass<String>(key)
inline fun Context.formParamAsString(key: String) = formParamAsClass<String>(key)
inline fun Context.headerAsString(key: String) = headerAsClass<String>(key)

inline fun Validator<String>.minLength(min: Int) = check(CharSequenceMinLengthCheck(min), "must have a length greater or equal to $min")
inline fun Validator<String>.maxLength(max: Int) = check(CharSequenceMaxLengthCheck(max), "must have a length less or equal to $max")
inline fun Validator<String>.notEmpty() = check(CharSequenceNotEmptyCheck, "should not be empty")
inline fun Validator<String>.notBlank() = check(CharSequenceNotBlankCheck, "should not be blank")
inline fun Validator<String>.regex(regex: Regex) = check(CharSequenceRegexCheck(regex), "regex does not match")

@PublishedApi
internal data class CharSequenceMinLengthCheck(private val min: Int) : Check<CharSequence> {

    override fun invoke(p: CharSequence) = p.length >= min
}

@PublishedApi
internal data class CharSequenceMaxLengthCheck(private val max: Int) : Check<CharSequence> {

    override fun invoke(p: CharSequence) = p.length <= max
}

@PublishedApi
internal data object CharSequenceNotEmptyCheck : Check<CharSequence> {

    override fun invoke(p: CharSequence) = p.isNotEmpty()
}

@PublishedApi
internal data object CharSequenceNotBlankCheck : Check<CharSequence> {

    override fun invoke(p: CharSequence) = p.isNotEmpty()
}

@PublishedApi
internal data class CharSequenceRegexCheck(private val regex: Regex) : Check<CharSequence> {

    override fun invoke(p: CharSequence) = regex.matches(p)
}
