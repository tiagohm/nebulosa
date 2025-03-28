package nebulosa.api.alignment.polar.tppa

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.api.mounts.MountEventAware
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.function.Consumer

class TPPAExecutor(
    private val messageService: MessageService,
    private val executorService: ExecutorService,
    eventBus: EventBus,
) : Consumer<MessageEvent>, CameraEventAware, MountEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<TPPAJob>(1)

    init {
        eventBus.register(this)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleMountEvent(event: MountEvent) {
        jobs.find { it.mount === event.device }?.handleMountEvent(event)
    }

    @Synchronized
    fun execute(camera: Camera, mount: Mount, request: TPPAStartRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(mount.connected) { "${mount.name} Mount is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} TPPA Job is already in progress" }
        check(jobs.none { it.mount === mount }) { "${camera.name} TPPA Job is already in progress" }

        val solver = request.plateSolver.get()

        with(TPPAJob(this, camera, solver, request, mount)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this) }
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.camera === camera }?.stop()
    }

    fun pause(camera: Camera) {
        jobs.find { it.camera === camera }?.pause()
    }

    fun unpause(camera: Camera) {
        jobs.find { it.camera === camera }?.unpause()
    }

    fun status(camera: Camera): TPPAEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
