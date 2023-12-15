package nebulosa.api.beans.annotations

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AngleParam(
    val name: String = "",
    val required: Boolean = true,
    val isHours: Boolean = false,
    val defaultValue: String = "",
)
