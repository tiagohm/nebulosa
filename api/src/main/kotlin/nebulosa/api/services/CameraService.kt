package nebulosa.api.services

import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.exceptions.DeviceNotFound
import org.springframework.stereotype.Service

@Service
class CameraService(private val cameraManager: CameraManager) {

    fun list(): List<CameraResponse> {
        return cameraManager.map(::CameraResponse)
    }

    operator fun get(name: String): CameraResponse {
        val camera = cameraManager.firstOrNull { it.name == name }
        return camera?.let(::CameraResponse) ?: throw DeviceNotFound
    }
}
