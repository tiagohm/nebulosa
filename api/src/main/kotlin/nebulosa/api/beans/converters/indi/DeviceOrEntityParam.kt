package nebulosa.api.beans.converters.indi

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DeviceOrEntityParam(val required: Boolean = true)
