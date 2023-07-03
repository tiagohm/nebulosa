package nebulosa.api.services

import nebulosa.api.components.CameraManager
import nebulosa.api.data.responses.CameraResponse
import org.springframework.stereotype.Service

@Service
class CameraService(private val cameraManager: CameraManager) {

    fun list(): List<CameraResponse> {
        return cameraManager.map(::CameraResponse)
    }

    operator fun get(name: String): CameraResponse {
        val camera = cameraManager.first { it.name == name }
        return CameraResponse(camera)
    }

    fun connect(name: String) {
        val camera = cameraManager.first { it.name == name }
        camera.connect()
    }

    fun disconnect(name: String) {
        val camera = cameraManager.first { it.name == name }
        camera.disconnect()
    }
}
