package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.events.CameraCaptureProgressChanged
import nebulosa.indi.device.ConnectionEvent
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.DevicePropertyEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
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
        sendMessage(DEVICE_PROPERTY_CHANGED, event.property)
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        sendMessage(DEVICE_PROPERTY_DELETED, event.property)
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        sendMessage(DEVICE_MESSAGE_RECEIVED, event)
    }

    // CAMERA

    fun sendSavedCameraImageEvent(event: SavedCameraImageEntity) {
        sendMessage(CAMERA_IMAGE_SAVED, event)
    }

    fun sendCameraUpdated(camera: Camera) {
        sendMessage(CAMERA_UPDATED, camera)
    }

    fun sendCameraCaptureProgressChanged(event: CameraCaptureProgressChanged) {
        sendMessage(CAMERA_CAPTURE_PROGRESS_CHANGED, event)
    }

    fun sendCameraCaptureFinished(event: CameraCaptureFinished) {
        sendMessage(CAMERA_CAPTURE_FINISHED, event)
    }

    fun sendCameraAttached(event: CameraAttached) {
        sendMessage(CAMERA_ATTACHED, event.device)
    }

    fun sendCameraDetached(event: CameraDetached) {
        sendMessage(CAMERA_DETACHED, event.device)
    }

    // MOUNT

    fun sendMountUpdated(mount: Mount) {
        sendMessage(MOUNT_UPDATED, mount)
    }

    fun sendMountAttached(event: MountAttached) {
        sendMessage(MOUNT_ATTACHED, event.device)
    }

    fun sendMountDetached(event: MountDetached) {
        sendMessage(MOUNT_DETACHED, event.device)
    }

    // FOCUSER

    fun sendFocuserUpdated(focuser: Focuser) {
        sendMessage(FOCUSER_UPDATED, focuser)
    }

    fun sendFocuserAttached(event: FocuserAttached) {
        sendMessage(FOCUSER_ATTACHED, event.device)
    }

    fun sendFocuserDetached(event: FocuserDetached) {
        sendMessage(FOCUSER_DETACHED, event.device)
    }

    // FILTER WHEEL

    fun sendFilterWheelUpdated(filterWheel: FilterWheel) {
        sendMessage(FILTER_WHEEL_UPDATED, filterWheel)
    }

    fun sendFilterWheelAttached(event: FilterWheelAttached) {
        sendMessage(FILTER_WHEEL_ATTACHED, event.device)
    }

    fun sendFilterWheelDetached(event: FilterWheelDetached) {
        sendMessage(FILTER_WHEEL_DETACHED, event.device)
    }

    // GUIDE OUTPUT

    fun sendGuideOutputUpdated(guideOutput: GuideOutput) {
        sendMessage(GUIDE_OUTPUT_UPDATED, guideOutput)
    }

    fun sendGuideOutputAttached(event: GuideOutputAttached) {
        sendMessage(GUIDE_OUTPUT_ATTACHED, event.device)
    }

    fun sendGuideOutputDetached(event: GuideOutputDetached) {
        sendMessage(GUIDE_OUTPUT_DETACHED, event.device)
    }

    // DEVICE

    fun sendConnectionEvent(event: ConnectionEvent) {
        val device = event.device ?: return

        when (device) {
            is Camera -> sendMessage(CAMERA_UPDATED, device)
            is Mount -> sendMessage(MOUNT_UPDATED, device)
            is Focuser -> sendMessage(FOCUSER_UPDATED, device)
            is FilterWheel -> sendMessage(FILTER_WHEEL_UPDATED, device)
        }

        if (device is GuideOutput) sendMessage(GUIDE_OUTPUT_UPDATED, device)
    }

    @Synchronized
    fun registerEventName(eventName: String) {
        registeredEventNames.addAll(eventName.mapEventName())
        LOG.info("registered event. name={}", eventName)
    }

    @Synchronized
    fun unregisterEventName(eventName: String) {
        registeredEventNames.removeAll(eventName.mapEventName())
        LOG.info("unregistered event. name={}", eventName)
    }

    @Synchronized
    private fun sendMessage(eventName: String, payload: Any) {
        if (eventName in registeredEventNames) {
            simpleMessageTemplate.convertAndSend(eventName, payload)
            LOG.info("event sent. name={}, payload={}", eventName, payload)
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE_PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE_PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE_MESSAGE_RECEIVED"
        const val CAMERA_IMAGE_SAVED = "CAMERA_IMAGE_SAVED"
        const val CAMERA_UPDATED = "CAMERA_UPDATED"
        const val CAMERA_CAPTURE_PROGRESS_CHANGED = "CAMERA_CAPTURE_PROGRESS_CHANGED"
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
        const val GUIDE_OUTPUT_UPDATED = "GUIDE_OUTPUT_UPDATED"
        const val GUIDE_OUTPUT_ATTACHED = "GUIDE_OUTPUT_ATTACHED"
        const val GUIDE_OUTPUT_DETACHED = "GUIDE_OUTPUT_DETACHED"

        @JvmStatic private val LOG = loggerFor<WebSocketService>()

        @JvmStatic private val DEVICE_EVENT_NAMES = setOf(
            DEVICE_PROPERTY_CHANGED,
            DEVICE_PROPERTY_DELETED,
            DEVICE_MESSAGE_RECEIVED,
        )

        @JvmStatic private val CAMERA_EVENT_NAMES = setOf(
            CAMERA_IMAGE_SAVED,
            CAMERA_UPDATED,
            CAMERA_CAPTURE_PROGRESS_CHANGED,
            CAMERA_CAPTURE_FINISHED,
            CAMERA_ATTACHED,
            CAMERA_DETACHED,
        )

        @JvmStatic private val MOUNT_EVENT_NAMES = setOf(
            MOUNT_UPDATED,
            MOUNT_ATTACHED,
            MOUNT_DETACHED,
        )

        @JvmStatic private val FOCUSER_EVENT_NAMES = setOf(
            FOCUSER_UPDATED,
            FOCUSER_ATTACHED,
            FOCUSER_DETACHED,
        )

        @JvmStatic private val FILTER_WHEEL_EVENT_NAMES = setOf(
            FILTER_WHEEL_UPDATED,
            FILTER_WHEEL_ATTACHED,
            FILTER_WHEEL_DETACHED,
        )

        @JvmStatic private val GUIDE_OUTPUT_EVENT_NAMES = setOf(
            GUIDE_OUTPUT_UPDATED,
            GUIDE_OUTPUT_ATTACHED,
            GUIDE_OUTPUT_DETACHED,
        )

        @JvmStatic private val ALL_EVENT_NAMES = listOf(
            DEVICE_EVENT_NAMES, CAMERA_EVENT_NAMES, MOUNT_EVENT_NAMES,
            FOCUSER_EVENT_NAMES, FILTER_WHEEL_EVENT_NAMES,
            GUIDE_OUTPUT_EVENT_NAMES,
        ).flatten().toSet()

        @JvmStatic
        private fun String.mapEventName() = when (this) {
            "ALL" -> ALL_EVENT_NAMES
            "DEVICE" -> DEVICE_EVENT_NAMES
            "CAMERA" -> CAMERA_EVENT_NAMES
            "MOUNT" -> MOUNT_EVENT_NAMES
            "FOCUSER" -> FOCUSER_EVENT_NAMES
            "FILTER_WHEEL" -> FILTER_WHEEL_EVENT_NAMES
            "GUIDE_OUTPUT" -> GUIDE_OUTPUT_EVENT_NAMES
            else -> setOf(this)
        }
    }
}
