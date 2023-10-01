package nebulosa.api.beans

import jakarta.servlet.http.HttpServletRequest
import nebulosa.api.beans.annotations.DateAndTime
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class DateAndTimeMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(DateAndTime::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val dateAndTime = parameter.getParameterAnnotation(DateAndTime::class.java)!!

        val dateValue = webRequest.pathVariables()["date"]
            ?: webRequest.getParameter("date")
        val timeValue = webRequest.pathVariables()["time"]
            ?: webRequest.getParameter("time")

        val date = dateValue?.ifBlank { null }
            ?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern(dateAndTime.datePattern)) }
            ?: LocalDate.now()

        val time = timeValue?.ifBlank { null }
            ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern(dateAndTime.timePattern)) }
            ?: LocalTime.now()

        return LocalDateTime.of(date, time)
            .let { if (dateAndTime.noSeconds) it.withSecond(0).withNano(0) else it }
    }

    companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        private fun NativeWebRequest.pathVariables(): Map<String, String> {
            val httpServletRequest = getNativeRequest(HttpServletRequest::class.java)!!
            return httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, String>
        }
    }
}
