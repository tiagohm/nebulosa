package nebulosa.api.beans

import jakarta.servlet.http.HttpServletRequest
import nebulosa.api.atlas.*
import nebulosa.api.beans.annotations.EntityBy
import nebulosa.api.connection.ConnectionService
import nebulosa.api.locations.LocationEntity
import nebulosa.api.locations.LocationRepository
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.core.MethodParameter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping

@Component
class EntityByMethodArgumentResolver(
    private val locationRepository: LocationRepository,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val satelliteRepository: SatelliteRepository,
    private val connectionService: ConnectionService,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(EntityBy::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val parameterName = parameter.parameterName ?: "id"
        val parameterValue = webRequest.pathVariables()[parameterName]
            ?: webRequest.getParameter(parameterName)
            ?: return null

        return when (parameter.parameterType) {
            LocationEntity::class.java -> locationRepository.findByIdOrNull(parameterValue.toLong())
            StarEntity::class.java -> starRepository.findByIdOrNull(parameterValue.toLong())
            DeepSkyObjectEntity::class.java -> deepSkyObjectRepository.findByIdOrNull(parameterValue.toLong())
            SatelliteEntity::class.java -> satelliteRepository.findByIdOrNull(parameterValue.toLong())
            Camera::class.java -> connectionService.camera(parameterValue)
            Mount::class.java -> connectionService.mount(parameterValue)
            Focuser::class.java -> connectionService.focuser(parameterValue)
            FilterWheel::class.java -> connectionService.wheel(parameterValue)
            GuideOutput::class.java -> connectionService.guideOutput(parameterValue)
            else -> null
        }
    }

    companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        private fun NativeWebRequest.pathVariables(): Map<String, String> {
            val httpServletRequest = getNativeRequest(HttpServletRequest::class.java)!!
            return httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, String>
        }
    }
}