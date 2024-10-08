package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.api.rotators.RotatorEventAware
import nebulosa.api.wheels.WheelEventAware
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

class SequencerExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val executorService: ExecutorService,
    private val calibrationFrameService: CalibrationFrameService,
    eventBus: EventBus,
) : Consumer<MessageEvent>, CameraEventAware, WheelEventAware, FocuserEventAware, RotatorEventAware, Executor by executorService {

    private val jobs = ConcurrentHashMap.newKeySet<SequencerJob>(1)

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        jobs.find { it.focuser === event.device }?.handleFocuserEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleRotatorEvent(event: RotatorEvent) {
        jobs.find { it.rotator === event.device }?.handleRotatorEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    fun execute(
        camera: Camera, request: SequencerPlanRequest,
        mount: Mount? = null, wheel: FilterWheel? = null, focuser: Focuser? = null, rotator: Rotator? = null,
    ) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} Sequencer Job is already in progress" }

        if (wheel != null && wheel.connected) {
            check(jobs.none { it.wheel === wheel }) { "${camera.name} Sequencer Job is already in progress" }
        }

        if (focuser != null && focuser.connected) {
            check(jobs.none { it.focuser === focuser }) { "${camera.name} Sequencer Job is already in progress" }
        }

        if (rotator != null && rotator.connected) {
            check(jobs.none { it.rotator === rotator }) { "${camera.name} Sequencer Job is already in progress" }
        }

        with(SequencerJob(this, camera, request, guider, mount, wheel, focuser, rotator, calibrationFrameService)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this) }
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

    fun status(camera: Camera): SequencerEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
