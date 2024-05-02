package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.api.solver.PlateSolverService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
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

    private val jobs = ConcurrentHashMap.newKeySet<TPPAJob>(1)

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
        check(mount.connected) { "${mount.name} Mount is not connected" }
        check(jobs.any { it.task.camera === camera }) { "${camera.name} TPPA Job is already in progress" }
        check(jobs.any { it.task.mount === mount }) { "${camera.name} TPPA Job is already in progress" }

        val solver = plateSolverService.solverFor(request.plateSolver)
        val task = TPPATask(camera, solver, request, mount)
        task.subscribe(this)

        with(TPPAJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }

    fun pause(camera: Camera) {
        jobs.find { it.task.camera === camera }?.pause()
    }

    fun unpause(camera: Camera) {
        jobs.find { it.task.camera === camera }?.unpause()
    }
}
