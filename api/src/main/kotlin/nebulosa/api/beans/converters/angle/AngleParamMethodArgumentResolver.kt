package nebulosa.api.beans.converters.angle

import nebulosa.api.beans.converters.annotation
import nebulosa.api.beans.converters.hasAnnotation
import nebulosa.api.beans.converters.parameter
import nebulosa.math.Angle
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AngleParamMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasAnnotation<AngleParam>()
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Angle {
        val param = parameter.annotation<AngleParam>()!!
        val parameterName = param.name.ifBlank { null } ?: parameter.parameterName!!
        val parameterValue = webRequest.parameter(parameterName) ?: param.defaultValue
        return Angle(parameterValue, param.isHours)
    }
}
