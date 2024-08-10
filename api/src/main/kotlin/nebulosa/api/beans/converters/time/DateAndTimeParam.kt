package nebulosa.api.beans.converters.time

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DateAndTimeParam(
    val datePattern: String = "yyyy-MM-dd",
    val timePattern: String = "HH:mm:ss",
    val noSeconds: Boolean = true,
    val nullable: Boolean = false,
)
