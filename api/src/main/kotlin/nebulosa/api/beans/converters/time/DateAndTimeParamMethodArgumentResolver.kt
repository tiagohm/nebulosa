package nebulosa.api.beans.converters.time

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
import java.time.temporal.Temporal

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
    ): Temporal? {
        val dateAndTimeParam = parameter.annotation<DateAndTimeParam>()!!
        val type = parameter.parameterType

        val dateValue = webRequest.parameter("date")?.ifBlank { null }
        val timeValue = webRequest.parameter("time")?.ifBlank { null }

        val date = dateValue
            ?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern(dateAndTimeParam.datePattern)) }
            ?: if (dateAndTimeParam.nullable) null else LocalDate.now()

        if (type === LocalDate::class.java) return date

        val time = timeValue
            ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern(dateAndTimeParam.timePattern)) }
            ?: if (dateAndTimeParam.nullable) null else LocalTime.now()

        if (type === LocalTime::class.java) return time

        return LocalDateTime.of(date ?: LocalDate.now(), time ?: LocalTime.now())
            .let { if (dateAndTimeParam.noSeconds) it.withSecond(0).withNano(0) else it }
    }
}
