package nebulosa.server.equipments.cameras

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraDetached
import nebulosa.indi.devices.cameras.CameraEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap

class CameraService : KoinComponent {

    private val eventBus by inject<EventBus>()
    private val cameras = ConcurrentHashMap<String, Camera>(4)
    private val cameraTasks = ConcurrentHashMap<Camera, MutableList<CameraExposureTask>>(2)

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraAttached -> {
                cameras[event.device.name] = event.device
            }
            is CameraDetached -> {
                cameras.remove(event.device.name)
                cameraTasks.remove(event.device)
            }
//            is CameraCaptureStartedEvent -> {
//
//            }
//            is CameraCaptureSavedEvent -> {
//                val latestCapturePath = "${event.path}"
//                val latestCaptureDate = System.currentTimeMillis()
//                event.device["latestCapturePath"] = latestCapturePath
//                event.device["latestCaptureDate"] = latestCaptureDate
//
//                if (!event.isTemporary) {
//                    box.put(CameraCaptureHistory(name = event.device.name, path = latestCapturePath, savedAt = latestCaptureDate))
//                }
//            }
//            is CameraCaptureFinishedEvent -> {
//
//            }
//            else -> {
//                val task = runningTask.get() ?: return
//
//                if (task.camera === event.device) {
//                    task.onCameraEventReceived(event)
//                }
//            }
        }
    }

    fun list() = cameras.values.toList()
}
