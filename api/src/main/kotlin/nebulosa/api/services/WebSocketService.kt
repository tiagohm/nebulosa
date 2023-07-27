package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.data.responses.FocuserResponse
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.DevicePropertyEvent
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.focuser.Focuser
import nebulosa.log.loggerFor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketService(private val simpleMessageTemplate: SimpMessagingTemplate) {

    private val registeredEventNames = HashSet<String>()

    // INDI

    fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        sendINDIPropertyEvent(DEVICE_PROPERTY_CHANGED, event.property)
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        sendINDIPropertyEvent(DEVICE_PROPERTY_DELETED, event.property)
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        sendMessage(DEVICE_MESSAGE_RECEIVED, mapOf("device" to event.device?.name, "message" to event.message))
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendINDIPropertyEvent(eventName: String, property: PropertyVector<*, *>) {
        sendMessage(eventName, INDIPropertyResponse(property))
    }

    // CAMERA

    fun sendSavedCameraImageEvent(event: SavedCameraImageEntity) {
        sendMessage(CAMERA_IMAGE_SAVED, event)
    }

    fun sendCameraUpdated(camera: Camera) {
        sendCameraEvent(CAMERA_UPDATED, camera)
    }

    fun sendCameraCaptureFinished(camera: Camera) {
        sendCameraEvent(CAMERA_CAPTURE_FINISHED, camera)
    }

    fun sendCameraAttached(camera: Camera) {
        sendCameraEvent(CAMERA_ATTACHED, camera)
    }

    fun sendCameraDetached(camera: Camera) {
        sendCameraEvent(CAMERA_DETACHED, camera)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendCameraEvent(eventName: String, camera: Camera) {
        sendMessage(eventName, CameraResponse(camera))
    }

    // FOCUSER

    fun sendFocuserUpdated(focuser: Focuser) {
        sendFocuserEvent(FOCUSER_UPDATED, focuser)
    }

    fun sendFocuserAttached(focuser: Focuser) {
        sendFocuserEvent(FOCUSER_ATTACHED, focuser)
    }

    fun sendFocuserDetached(focuser: Focuser) {
        sendFocuserEvent(FOCUSER_DETACHED, focuser)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendFocuserEvent(eventName: String, focuser: Focuser) {
        sendMessage(eventName, FocuserResponse(focuser))
    }

    fun registerEventName(eventName: String) {
        registeredEventNames.addAll(eventName.mapEventName())
        LOG.info("registered event. name={}", eventName)
    }

    fun unregisterEventName(eventName: String) {
        registeredEventNames.removeAll(eventName.mapEventName())
        LOG.info("unregistered event. name={}", eventName)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, payload: Any) {
        if (eventName in registeredEventNames) {
            simpleMessageTemplate.convertAndSend(eventName, payload)
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE_PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE_PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE_MESSAGE_RECEIVED"
        const val CAMERA_IMAGE_SAVED = "CAMERA_IMAGE_SAVED"
        const val CAMERA_UPDATED = "CAMERA_UPDATED"
        const val CAMERA_CAPTURE_FINISHED = "CAMERA_CAPTURE_FINISHED"
        const val CAMERA_ATTACHED = "CAMERA_ATTACHED"
        const val CAMERA_DETACHED = "CAMERA_DETACHED"
        const val FOCUSER_UPDATED = "FOCUSER_UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER_ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER_DETACHED"

        @JvmStatic private val LOG = loggerFor<WebSocketService>()

        @JvmStatic private val DEVICE_EVENT_NAMES = setOf(
            DEVICE_PROPERTY_CHANGED,
            DEVICE_PROPERTY_DELETED,
            DEVICE_MESSAGE_RECEIVED,
        )

        @JvmStatic private val CAMERA_EVENT_NAMES = setOf(
            CAMERA_IMAGE_SAVED,
            CAMERA_UPDATED,
            CAMERA_CAPTURE_FINISHED,
            CAMERA_ATTACHED,
            CAMERA_DETACHED,
        )

        @JvmStatic private val FOCUSER_EVENT_NAMES = setOf(
            FOCUSER_UPDATED,
            FOCUSER_ATTACHED,
            FOCUSER_DETACHED,
        )

        @JvmStatic private val ALL_EVENT_NAMES = listOf(
            DEVICE_EVENT_NAMES, CAMERA_EVENT_NAMES,
            FOCUSER_EVENT_NAMES,
        ).flatten().toSet()

        @JvmStatic
        private fun String.mapEventName() = when (this) {
            "ALL" -> ALL_EVENT_NAMES
            "DEVICE" -> DEVICE_EVENT_NAMES
            "CAMERA" -> CAMERA_EVENT_NAMES
            "FOCUSER" -> FOCUSER_EVENT_NAMES
            else -> setOf(this)
        }
    }
}
