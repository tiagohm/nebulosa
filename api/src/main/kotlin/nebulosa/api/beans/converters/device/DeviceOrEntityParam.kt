package nebulosa.api.beans.converters.device

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeviceOrEntityParam(
    val name: String = "",
    val defaultValue: String = ""
)
