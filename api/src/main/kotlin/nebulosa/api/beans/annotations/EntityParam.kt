package nebulosa.api.beans.annotations

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class EntityParam(val required: Boolean = true)
