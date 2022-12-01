package nebulosa.api.cameras

import nebulosa.api.scheduler.ScheduledTaskFinishedEvent
import nebulosa.api.scheduler.ScheduledTaskStartedEvent
import nebulosa.api.scheduler.SchedulerService
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.events.CameraAttachedEvent
import nebulosa.indi.devices.events.CameraDetachedEvent
import nebulosa.indi.devices.events.CameraEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Service
class CameraService {

    @Autowired
    private lateinit var schedulerService: SchedulerService

    private val cameras = ConcurrentHashMap<String, Camera>()
    private val runningTask = AtomicReference<CameraCaptureTask>(null)
    private val runningTasks = ArrayList<CameraCaptureTask>(8)

    @EventListener
    fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraAttachedEvent -> {
                cameras[event.device.name] = event.device
            }
            is CameraDetachedEvent -> {
                cameras.remove(event.device.name)
            }
            else -> {
                val task = runningTask.get() ?: return

                if (task.camera === event.device) {
                    task.onCameraEventReceived(event)
                }
            }
        }
    }

    @EventListener
    fun onScheduledTaskStartedEvent(event: ScheduledTaskStartedEvent) {
        if (event.task in runningTasks) {
            runningTask.set(event.task as CameraCaptureTask)
        }
    }

    @EventListener
    fun onScheduledTaskFinishedEvent(event: ScheduledTaskFinishedEvent) {
        if (event.task in runningTasks) {
            runningTask.set(null)
        }
    }

    fun cameras(): List<CameraRes> {
        return cameras.values.map(CameraRes::from)
    }

    fun camera(name: String): CameraRes {
        return cameras[name]!!.let(CameraRes::from)
    }

    fun connect(name: String) {
        cameras[name]!!.connect()
    }

    fun disconnect(name: String) {
        cameras[name]!!.disconnect()
    }

    @Synchronized
    fun startCapture(name: String, startCaptureReq: StartCaptureReq) {
        val camera = cameras[name]!!
        val task = CameraCaptureTask(
            camera,
            startCaptureReq.exposureInMicros, startCaptureReq.amount, startCaptureReq.delay,
            startCaptureReq.x, startCaptureReq.y, startCaptureReq.width, startCaptureReq.height,
            camera.frameFormats.first { it.name == startCaptureReq.frameFormat },
            startCaptureReq.frameType, startCaptureReq.binX, startCaptureReq.binY,
        )
        runningTasks.add(task)
        schedulerService.add(task)
    }

    @Synchronized
    fun stopCapture(name: String) {
        runningTasks.forEach { it.cancel() }
        runningTask.set(null)
    }
}
