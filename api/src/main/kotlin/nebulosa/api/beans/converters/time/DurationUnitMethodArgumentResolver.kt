package nebulosa.api.beans.converters.time

import nebulosa.api.beans.converters.annotation
import nebulosa.api.beans.converters.hasAnnotation
import nebulosa.api.beans.converters.parameter
import org.springframework.boot.convert.DurationUnit
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.Duration

@Component
class DurationUnitMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasAnnotation<DurationUnit>()
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val unit = parameter.annotation<DurationUnit>()!!.value
        val value = webRequest.parameter(parameter.parameterName!!) ?: return null
        return Duration.of(value.toLong(), unit)
    }
}
