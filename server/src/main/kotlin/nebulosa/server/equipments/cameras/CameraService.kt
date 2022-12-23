package nebulosa.server.equipments.cameras

import io.grpc.stub.StreamObserver
import nebulosa.grpc.CameraExposureTaskResponse
import nebulosa.indi.devices.cameras.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

class CameraService : KoinComponent {

    private val eventBus by inject<EventBus>()
    private val executor by inject<ExecutorService>()
    private val cameras = ConcurrentHashMap<String, Camera>(4)
    private val imagingCameraTasks = ConcurrentHashMap<Camera, MutableList<CameraExposureTask>>(2)

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
                imagingCameraTasks.remove(event.device)
            }
        }
    }

    fun list() = cameras.values.toList()

    fun startExposure(
        camera: Camera,
        exposure: Long, amount: Int, delay: Long,
        x: Int, y: Int, width: Int, height: Int,
        frameFormat: String, frameType: FrameType,
        binX: Int, binY: Int,
        save: Boolean, savePath: String, autoSubFolderMode: AutoSubFolderMode,
        responseObserver: StreamObserver<CameraExposureTaskResponse>,
    ) {
        val task = CameraExposureTask(
            camera, exposure, amount, delay, x, y, width, height,
            frameFormat, frameType, binX, binY, save, savePath,
            autoSubFolderMode, responseObserver,
        )

        executor.submit(task)
    }

    @Synchronized
    fun stopExposure(camera: Camera) {
        camera.abortCapture()
    }
}
