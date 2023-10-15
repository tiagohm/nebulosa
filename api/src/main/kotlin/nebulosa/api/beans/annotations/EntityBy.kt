package nebulosa.api.beans.annotations

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class EntityBy(val required: Boolean = true)
