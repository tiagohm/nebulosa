package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.DevicePropertyEvent
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
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
        sendMessage(DEVICE_MESSAGE_RECEIVED, event)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendINDIPropertyEvent(eventName: String, property: PropertyVector<*, *>) {
        sendMessage(eventName, property)
    }

    // CAMERA

    fun sendSavedCameraImageEvent(event: SavedCameraImageEntity) {
        sendMessage(CAMERA_IMAGE_SAVED, event)
    }

    fun sendCameraUpdated(camera: Camera) {
        sendCameraEvent(CAMERA_UPDATED, camera)
    }

    fun sendCameraCaptureFinished(event: CameraCaptureFinished) {
        sendMessage(CAMERA_CAPTURE_FINISHED, event)
    }

    fun sendCameraAttached(event: CameraAttached) {
        sendCameraEvent(CAMERA_ATTACHED, event.device)
    }

    fun sendCameraDetached(event: CameraDetached) {
        sendCameraEvent(CAMERA_DETACHED, event.device)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendCameraEvent(eventName: String, camera: Camera) {
        sendMessage(eventName, camera)
    }

    // MOUNT

    fun sendMountUpdated(mount: Mount) {
        sendMountEvent(MOUNT_UPDATED, mount)
    }

    fun sendMountAttached(event: MountAttached) {
        sendMountEvent(MOUNT_ATTACHED, event.device)
    }

    fun sendMountDetached(event: MountDetached) {
        sendMountEvent(MOUNT_DETACHED, event.device)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMountEvent(eventName: String, mount: Mount) {
        sendMessage(eventName, mount)
    }

    // FOCUSER

    fun sendFocuserUpdated(focuser: Focuser) {
        sendFocuserEvent(FOCUSER_UPDATED, focuser)
    }

    fun sendFocuserAttached(event: FocuserAttached) {
        sendFocuserEvent(FOCUSER_ATTACHED, event.device)
    }

    fun sendFocuserDetached(event: FocuserDetached) {
        sendFocuserEvent(FOCUSER_DETACHED, event.device)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendFocuserEvent(eventName: String, focuser: Focuser) {
        sendMessage(eventName, focuser)
    }

    // FILTER WHEEL

    fun sendFilterWheelUpdated(filterWheel: FilterWheel) {
        sendFilterWheelEvent(FILTER_WHEEL_UPDATED, filterWheel)
    }

    fun sendFilterWheelAttached(event: FilterWheelAttached) {
        sendFilterWheelEvent(FILTER_WHEEL_ATTACHED, event.device)
    }

    fun sendFilterWheelDetached(event: FilterWheelDetached) {
        sendFilterWheelEvent(FILTER_WHEEL_DETACHED, event.device)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendFilterWheelEvent(eventName: String, filterWheel: FilterWheel) {
        sendMessage(eventName, filterWheel)
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
        const val MOUNT_UPDATED = "MOUNT_UPDATED"
        const val MOUNT_ATTACHED = "MOUNT_ATTACHED"
        const val MOUNT_DETACHED = "MOUNT_DETACHED"
        const val FOCUSER_UPDATED = "FOCUSER_UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER_ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER_DETACHED"
        const val FILTER_WHEEL_UPDATED = "FILTER_WHEEL_UPDATED"
        const val FILTER_WHEEL_ATTACHED = "FILTER_WHEEL_ATTACHED"
        const val FILTER_WHEEL_DETACHED = "FILTER_WHEEL_DETACHED"

        @JvmStatic
        private val LOG = loggerFor<WebSocketService>()

        @JvmStatic
        private val DEVICE_EVENT_NAMES = setOf(
            DEVICE_PROPERTY_CHANGED,
            DEVICE_PROPERTY_DELETED,
            DEVICE_MESSAGE_RECEIVED,
        )

        @JvmStatic
        private val CAMERA_EVENT_NAMES = setOf(
            CAMERA_IMAGE_SAVED,
            CAMERA_UPDATED,
            CAMERA_CAPTURE_FINISHED,
            CAMERA_ATTACHED,
            CAMERA_DETACHED,
        )

        @JvmStatic
        private val MOUNT_EVENT_NAMES = setOf(
            MOUNT_UPDATED,
            MOUNT_ATTACHED,
            MOUNT_DETACHED,
        )

        @JvmStatic
        private val FOCUSER_EVENT_NAMES = setOf(
            FOCUSER_UPDATED,
            FOCUSER_ATTACHED,
            FOCUSER_DETACHED,
        )

        @JvmStatic
        private val FILTER_WHEEL_EVENT_NAMES = setOf(
            FILTER_WHEEL_UPDATED,
            FILTER_WHEEL_ATTACHED,
            FILTER_WHEEL_DETACHED,
        )

        @JvmStatic
        private val ALL_EVENT_NAMES = listOf(
            DEVICE_EVENT_NAMES, CAMERA_EVENT_NAMES, MOUNT_EVENT_NAMES,
            FOCUSER_EVENT_NAMES, FILTER_WHEEL_EVENT_NAMES,
        ).flatten().toSet()

        @JvmStatic
        private fun String.mapEventName() = when (this) {
            "ALL" -> ALL_EVENT_NAMES
            "DEVICE" -> DEVICE_EVENT_NAMES
            "CAMERA" -> CAMERA_EVENT_NAMES
            "MOUNT" -> MOUNT_EVENT_NAMES
            "FOCUSER" -> FOCUSER_EVENT_NAMES
            "FILTER_WHEEL" -> FILTER_WHEEL_EVENT_NAMES
            else -> setOf(this)
        }
    }
}
