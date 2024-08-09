package nebulosa.api.beans.converters.angle

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AngleParam(
    val name: String = "",
    val required: Boolean = true,
    val isHours: Boolean = false,
    val defaultValue: String = "",
)
