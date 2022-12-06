package nebulosa.api.cameras

import io.objectbox.Box
import jakarta.validation.Valid
import nebulosa.api.scheduler.ScheduledTaskFinishedEvent
import nebulosa.api.scheduler.ScheduledTaskStartedEvent
import nebulosa.api.scheduler.SchedulerService
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.events.CameraAttachedEvent
import nebulosa.indi.devices.events.CameraDetachedEvent
import nebulosa.indi.devices.events.CameraEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@RestController
@RequestMapping("cameras")
class CameraController {

    private val cameras = ConcurrentHashMap<String, Camera>()
    private val runningTask = AtomicReference<CameraCaptureTask>(null)
    private val runningTasks = ArrayList<CameraCaptureTask>(8)

    @Autowired
    private lateinit var schedulerService: SchedulerService

    @Autowired
    private lateinit var appDirectory: Path

    @Autowired
    private lateinit var box: Box<CameraCaptureHistory>

    @EventListener
    fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraAttachedEvent -> {
                cameras[event.device.name] = event.device
            }
            is CameraDetachedEvent -> {
                cameras.remove(event.device.name)
            }
            is CameraCaptureStartedEvent -> {

            }
            is CameraCaptureSavedEvent -> {
                val latestCapturePath = "${event.path}"
                val latestCaptureDate = System.currentTimeMillis()
                event.device["latestCapturePath"] = latestCapturePath
                event.device["latestCaptureDate"] = latestCaptureDate
                box.put(CameraCaptureHistory(name = event.device.name, path = latestCapturePath, savedAt = latestCaptureDate))
            }
            is CameraCaptureFinishedEvent -> {

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

    @GetMapping
    fun list(): List<CameraDevice> {
        return cameras.values.map(CameraDevice::from)
    }

    @GetMapping("{name}")
    fun get(@PathVariable name: String): CameraDevice {
        return cameras[name]!!.let(CameraDevice::from)
    }

    @PostMapping("{name}/connect")
    fun connect(@PathVariable name: String) {
        cameras[name]!!.connect()
    }

    @PostMapping("{name}/disconnect")
    fun disconnect(@PathVariable name: String) {
        cameras[name]!!.disconnect()
    }

    @Synchronized
    @PostMapping("{name}/startcapture")
    fun startCapture(
        @PathVariable name: String,
        @RequestBody @Valid startCapture: StartCapture,
    ) {
        val camera = cameras[name]!!
        val task = CameraCaptureTask(
            camera,
            startCapture.exposure, startCapture.amount, startCapture.delay,
            startCapture.x, startCapture.y, startCapture.width, startCapture.height,
            camera.frameFormats.first { it.name == startCapture.frameFormat },
            startCapture.frameType, startCapture.binX, startCapture.binY,
            startCapture.save, startCapture.savePath.ifBlank { appDirectory.toString() },
            startCapture.autoSubFolderMode,
        )

        runningTasks.add(task)
        schedulerService.add(task)
    }

    @Synchronized
    @PostMapping("{name}/stopcapture")
    fun stopCapture(@PathVariable name: String) {
        runningTasks.forEach { it.cancel() }
        runningTask.set(null)
    }

    @GetMapping("{name}/history")
    fun history(@PathVariable name: String): List<CameraCaptureHistory> {
        val query = box.query(CameraCaptureHistory_.name.equal(name))
            .orderDesc(CameraCaptureHistory_.savedAt)
            .build()
        return query.use { query.find() }
    }
}
