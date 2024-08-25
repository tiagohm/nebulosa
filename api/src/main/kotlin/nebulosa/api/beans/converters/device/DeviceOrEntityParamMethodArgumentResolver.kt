package nebulosa.api.beans.converters.device

import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.beans.converters.annotation
import nebulosa.api.beans.converters.parameter
import nebulosa.api.calibration.CalibrationFrameEntity
import nebulosa.api.calibration.CalibrationFrameRepository
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.thermometer.Thermometer
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class DeviceOrEntityParamMethodArgumentResolver(
    private val satelliteRepository: SatelliteRepository,
    private val calibrationFrameRepository: CalibrationFrameRepository,
    private val connectionService: ConnectionService,
) : HandlerMethodArgumentResolver {

    private val entityResolvers = mapOf<Class<*>, (String) -> Any?>(
        SatelliteEntity::class.java to { satelliteRepository.find(it.toLong()) },
        CalibrationFrameEntity::class.java to { calibrationFrameRepository.find(it.toLong()) },
        Device::class.java to { connectionService.device(it) },
        Camera::class.java to { connectionService.camera(it) },
        Mount::class.java to { connectionService.mount(it) },
        Focuser::class.java to { connectionService.focuser(it) },
        FilterWheel::class.java to { connectionService.wheel(it) },
        Rotator::class.java to { connectionService.rotator(it) },
        GPS::class.java to { connectionService.gps(it) },
        GuideOutput::class.java to { connectionService.guideOutput(it) },
        DustCap::class.java to { connectionService.dustCap(it) },
        Thermometer::class.java to { connectionService.thermometer(it) },
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
        val requestParam = parameter.annotation<DeviceOrEntityParam>()
        val parameterName = requestParam?.name?.ifBlank { null } ?: parameter.parameterName ?: "id"
        val parameterValue = webRequest.parameter(parameterName) ?: requestParam?.defaultValue?.ifBlank { null }
        return entityByParameterValue(parameter.parameterType, parameterValue)
    }

    private fun entityByParameterValue(parameterType: Class<*>, parameterValue: String?): Any? {
        if (parameterValue.isNullOrBlank()) return null
        return entityResolvers[parameterType]?.invoke(parameterValue)
    }
}
