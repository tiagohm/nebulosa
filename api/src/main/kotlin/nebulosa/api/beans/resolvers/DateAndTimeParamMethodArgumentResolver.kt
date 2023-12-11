package nebulosa.api.beans.resolvers

import nebulosa.api.beans.annotations.DateAndTimeParam
import nebulosa.api.beans.converters.annotation
import nebulosa.api.beans.converters.hasAnnotation
import nebulosa.api.beans.converters.parameter
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class DateAndTimeParamMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasAnnotation<DateAndTimeParam>()
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val dateAndTimeParam = parameter.annotation<DateAndTimeParam>()!!

        val dateValue = webRequest.parameter("date")
        val timeValue = webRequest.parameter("time")

        val date = dateValue?.ifBlank { null }
            ?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern(dateAndTimeParam.datePattern)) }
            ?: LocalDate.now()

        val time = timeValue?.ifBlank { null }
            ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern(dateAndTimeParam.timePattern)) }
            ?: LocalTime.now()

        return LocalDateTime.of(date, time)
            .let { if (dateAndTimeParam.noSeconds) it.withSecond(0).withNano(0) else it }
    }
}
