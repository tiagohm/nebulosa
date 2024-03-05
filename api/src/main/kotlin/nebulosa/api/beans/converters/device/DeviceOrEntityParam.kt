package nebulosa.api.beans.converters.device

@Retention
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DeviceOrEntityParam(val required: Boolean = true)
