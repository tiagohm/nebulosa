@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.validation.BodyValidator
import io.javalin.validation.Check
import jakarta.validation.Validation
import nebulosa.log.loggerFor

inline fun <T> BodyValidator<T>.validate() = check(BodyValidatorCheck, "invalid body")

@PublishedApi
internal data object BodyValidatorCheck : Check<Any?> {

    override fun invoke(p: Any?) = p == null
            || VALIDATOR.validate(p).onEach { LOG.warn("{}: {}", it.propertyPath, it.message) }.isEmpty()

    private val VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory()
    private val VALIDATOR = VALIDATOR_FACTORY.validator
    private val LOG = loggerFor<BodyValidatorCheck>()
}
