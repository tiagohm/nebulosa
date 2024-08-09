package nebulosa.api.beans.converters.device

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DeviceOrEntityParam(
    val name: String = "",
    val defaultValue: String = ""
)
