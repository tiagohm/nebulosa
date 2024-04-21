package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.api.solver.PlateSolverService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class TPPAExecutor(
    private val messageService: MessageService,
    private val plateSolverService: PlateSolverService,
) : Consumer<TPPAEvent> {

    private val jobs = ConcurrentHashMap.newKeySet<TPPAJob>()

    override fun accept(event: TPPAEvent) {
        messageService.sendMessage(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        jobs.find { it.task.mount === event.device }?.handleMountEvent(event)
    }

    @Synchronized
    fun execute(camera: Camera, mount: Mount, request: TPPAStartRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(mount.connected) { "${mount.name} Guide Output is not connected" }
        check(jobs.any { it.task.camera === camera || it.task.mount === mount }) { "${camera.name}/${mount.name} TPPA Job in progress" }

        LOG.info { "starting TPPA. camera=$camera, mount=$mount, request=$request" }

        val solver = plateSolverService.solverFor(request.plateSolver)
        val task = TPPATask(camera, solver, request, mount)
        task.subscribe(this)

        with(TPPAJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera, mount: Mount) {
        jobs.find { it.task.camera === camera && it.task.mount === mount }
            ?.also(jobs::remove)
            ?.stop()
    }

    fun pause(camera: Camera, mount: Mount) {
        jobs.find { it.task.camera === camera && it.task.mount === mount }?.pause()
    }

    fun unpause(camera: Camera, mount: Mount) {
        jobs.find { it.task.camera === camera && it.task.mount === mount }?.unpause()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAExecutor>()
    }
}
