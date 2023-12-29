package nebulosa.api.beans.converters.time

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DateAndTimeParam(
    val datePattern: String = "yyyy-MM-dd",
    val timePattern: String = "HH:mm",
    val noSeconds: Boolean = true,
)
