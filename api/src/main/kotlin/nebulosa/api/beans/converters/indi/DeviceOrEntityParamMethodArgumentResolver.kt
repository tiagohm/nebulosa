package nebulosa.api.beans.converters.indi

import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.beans.converters.annotation
import nebulosa.api.beans.converters.parameter
import nebulosa.api.connection.ConnectionService
import nebulosa.api.locations.LocationEntity
import nebulosa.api.locations.LocationRepository
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException

@Component
class DeviceOrEntityParamMethodArgumentResolver(
    private val locationRepository: LocationRepository,
    private val satelliteRepository: SatelliteRepository,
    private val connectionService: ConnectionService,
) : HandlerMethodArgumentResolver {

    private val entityResolvers = mapOf<Class<*>, (String) -> Any?>(
        LocationEntity::class.java to { locationRepository.find(it.toLong()) },
        SatelliteEntity::class.java to { satelliteRepository.find(it.toLong()) },
        Device::class.java to { connectionService.device(it) },
        Camera::class.java to { connectionService.camera(it) },
        Mount::class.java to { connectionService.mount(it) },
        Focuser::class.java to { connectionService.focuser(it) },
        FilterWheel::class.java to { connectionService.wheel(it) },
        GuideOutput::class.java to { connectionService.guideOutput(it) },
    )

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType in entityResolvers
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val requestParam = parameter.annotation<RequestParam>()
        val parameterName = requestParam?.name?.ifBlank { null } ?: parameter.parameterName ?: "id"
        val parameterValue = webRequest.parameter(parameterName) ?: requestParam?.defaultValue

        val entity = entityByParameterValue(parameter.parameterType, parameterValue)

        if (requestParam != null && requestParam.required && entity == null) {
            val message = "Cannot found a ${parameter.parameterType.simpleName} entity with name [$parameterValue]"
            throw ResponseStatusException(HttpStatus.NOT_FOUND, message)
        }

        return entity
    }

    private fun entityByParameterValue(parameterType: Class<*>, parameterValue: String?): Any? {
        if (parameterValue.isNullOrBlank()) return null
        return entityResolvers[parameterType]?.invoke(parameterValue)
    }
}
