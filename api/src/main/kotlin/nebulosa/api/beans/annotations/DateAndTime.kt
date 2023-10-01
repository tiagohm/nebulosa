package nebulosa.api.beans.annotations

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DateAndTime(
    val datePattern: String = "yyyy-MM-dd",
    val timePattern: String = "HH:mm",
    val noSeconds: Boolean = true,
)
