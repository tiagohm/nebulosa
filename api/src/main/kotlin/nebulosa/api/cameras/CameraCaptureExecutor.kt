package nebulosa.api.cameras

import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.inject.Named
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.function.Consumer

class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val calibrationFrameService: CalibrationFrameService,
    private val guider: Guider,
    private val executorService: ExecutorService,
    eventBus: EventBus,
) : Consumer<CameraCaptureEvent>, CameraEventAware, WheelEventAware, KoinComponent, Executor by executorService {

    private val jobs = ConcurrentHashMap.newKeySet<CameraCaptureJob>(2)

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        jobs.find { it.wheel === event.device }?.handleFilterWheelEvent(event)
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(
        camera: Camera, request: CameraStartCaptureRequest,
        mount: Mount? = null, wheel: FilterWheel? = null, focuser: Focuser? = null, rotator: Rotator? = null,
    ) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} Camera Capture is already in progress" }

        val liveStackingManager = CameraLiveStackingManager(get(Named.liveStackingDir), calibrationFrameService)

        with(CameraCaptureJob(this, camera, request, guider, liveStackingManager, mount, wheel, focuser, rotator)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this); liveStackingManager.close() }
        }
    }

    fun pause(camera: Camera) {
        jobs.find { it.camera === camera }?.pause()
    }

    fun unpause(camera: Camera) {
        jobs.find { it.camera === camera }?.unpause()
    }

    fun stop(camera: Camera) {
        jobs.find { it.camera === camera }?.stop()
    }

    fun status(camera: Camera): CameraCaptureEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
