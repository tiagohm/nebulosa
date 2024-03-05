package nebulosa.api.beans.converters.location

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.atlas.Location
import nebulosa.api.beans.converters.hasAnnotation
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LocationParamMethodArgumentResolver(
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasAnnotation<LocationParam>()
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Location? {
        // val param = parameter.annotation<LocationParam>()!!
        val headerValue = webRequest.getHeader("X-Location")?.ifBlank { null } ?: return null
        return CACHED_LOCATIONS.getOrPut(headerValue) { objectMapper.readValue(headerValue, Location::class.java) }
    }

    companion object {

        @JvmStatic private val CACHED_LOCATIONS = HashMap<String, Location>(8)
    }
}
