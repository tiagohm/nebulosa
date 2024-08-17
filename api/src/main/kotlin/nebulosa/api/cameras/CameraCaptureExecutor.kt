package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.message.MessageService
import nebulosa.api.wheels.WheelEventAware
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val calibrationFrameService: CalibrationFrameService,
) : Consumer<CameraCaptureEvent>, CameraEventAware, WheelEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<CameraCaptureJob>(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        jobs.find { it.task.wheel === event.device }?.handleFilterWheelEvent(event)
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(
        camera: Camera, request: CameraStartCaptureRequest,
        mount: Mount? = null, wheel: FilterWheel? = null, focuser: Focuser? = null, rotator: Rotator? = null
    ) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.none { it.task.camera === camera }) { "${camera.name} Camera Capture is already in progress" }

        val liveStackingManager = CameraLiveStackingManager(calibrationFrameService)
        val task = CameraCaptureTask(
            camera, request, guider, false, threadPoolTaskExecutor,
            liveStackingManager, mount, wheel, focuser, rotator
        )

        task.subscribe(this)

        with(CameraCaptureJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this); liveStackingManager.close() }
            start()
        }
    }

    fun pause(camera: Camera) {
        jobs.find { it.task.camera === camera }?.pause()
    }

    fun unpause(camera: Camera) {
        jobs.find { it.task.camera === camera }?.unpause()
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }

    fun status(camera: Camera): CameraCaptureEvent? {
        return jobs.find { it.task.camera === camera }?.task?.get()
    }
}
