package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.indi.device.DevicePropertyEvent
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.camera.Camera
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketService(private val simpleMessageTemplate: SimpMessagingTemplate) {

    fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        sendINDIEvent("DEVICE_PROPERTY_CHANGED", event.property)
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        sendINDIEvent("DEVICE_PROPERTY_DELETED", event.property)
    }

    private fun sendINDIEvent(eventName: String, property: PropertyVector<*, *>) {
        simpleMessageTemplate.convertAndSend(eventName, INDIPropertyResponse(property))
    }

    fun sendSavedCameraImageEvent(event: SavedCameraImageEntity) {
        simpleMessageTemplate.convertAndSend("CAMERA_IMAGE_SAVED", event)
    }

    fun sendCameraUpdated(camera: Camera) {
        sendCameraEvent("CAMERA_UPDATED", camera)
    }

    fun sendCameraCaptureFinished(camera: Camera) {
        sendCameraEvent("CAMERA_CAPTURE_FINISHED", camera)
    }

    fun sendCameraAttached(camera: Camera) {
        sendCameraEvent("CAMERA_ATTACHED", camera)
    }

    fun sendCameraDetached(camera: Camera) {
        sendCameraEvent("CAMERA_DETACHED", camera)
    }

    private fun sendCameraEvent(eventName: String, camera: Camera) {
        simpleMessageTemplate.convertAndSend(eventName, CameraResponse(camera))
    }
}
