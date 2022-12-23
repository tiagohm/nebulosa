package nebulosa.server.equipments

import io.grpc.stub.StreamObserver
import nebulosa.grpc.CameraEquipment
import nebulosa.grpc.CameraExposureTaskResponse
import nebulosa.indi.devices.ConnectionType
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.FrameType
import nebulosa.server.equipments.cameras.AutoSubFolderMode
import nebulosa.server.equipments.cameras.CameraService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EquipmentService : KoinComponent {

    private val eventBus by inject<EventBus>()
    private val cameraService by inject<CameraService>()
    @Volatile private var deviceWaitingForConnection: Device? = null

    @Volatile @JvmField var imagingCamera: Camera? = null

    init {
        eventBus.register(this)
    }

    @Subscribe
    fun onDeviceEventReceived(event: DeviceEvent<*>) {
        if (event is DeviceConnected) {
            if (deviceWaitingForConnection === event.device) {
                deviceWaitingForConnection = null
            }
        }
    }

    @Synchronized
    fun open(
        name: String,
        deviceType: EquipmentType,
        connectionType: ConnectionType,
        connection: Any?,
    ): Device {
        when (deviceType) {
            EquipmentType.IMAGING_CAMERA -> {
                require(imagingCamera == null || imagingCamera?.name == name)
                if (imagingCamera?.name == name) return imagingCamera!!
                val camera = cameraService.list().firstOrNull { it.name == name }
                require(camera != null)

                if (!camera.isConnected) {
                    deviceWaitingForConnection = camera
                    camera.connect()

                    require(waitDeviceForConnection() && camera.isConnected)
                }

                imagingCamera = camera
                return camera
            }
            EquipmentType.MOUNT -> TODO()
            EquipmentType.GUIDING_CAMERA -> TODO()
            EquipmentType.FILTER_WHEEL -> TODO()
            EquipmentType.FOCUSER -> TODO()
            EquipmentType.ROTATOR -> TODO()
            EquipmentType.SWITCH -> TODO()
            EquipmentType.DOME -> TODO()
            EquipmentType.WEATHER -> TODO()
            EquipmentType.FLAT_PANEL -> TODO()
            EquipmentType.SAFETY_MONITOR -> TODO()
        }
    }

    fun cameraStartExposure(
        equipmentType: EquipmentType,
        exposure: Long, amount: Int, delay: Long,
        x: Int, y: Int, width: Int, height: Int,
        frameFormat: String, frameType: FrameType,
        binX: Int, binY: Int,
        save: Boolean, savePath: String, autoSubFolderMode: AutoSubFolderMode,
        responseObserver: StreamObserver<CameraExposureTaskResponse>,
    ) {
        val camera = if (equipmentType == EquipmentType.IMAGING_CAMERA) imagingCamera else null
        require(camera != null)
        cameraService.startExposure(
            camera, exposure, amount, delay,
            x, y, width, height,
            frameFormat, frameType, binX, binY,
            save, savePath, autoSubFolderMode,
            responseObserver
        )
    }

    fun cameraStopExposure(equipmentType: EquipmentType) {
        val camera = if (equipmentType == EquipmentType.IMAGING_CAMERA) imagingCamera else null
        require(camera != null)
        cameraService.stopExposure(camera)
    }

    private fun waitDeviceForConnection(): Boolean {
        var counter = 30

        while (counter > 0 && deviceWaitingForConnection != null) {
            Thread.sleep(1000L)
            counter--
        }

        return counter > 0L && deviceWaitingForConnection == null
    }

    companion object {

        @JvmStatic
        fun Camera.toCameraEquipment() = CameraEquipment.newBuilder()
            .setName(name)
            .setConnected(isConnected)
            .setHasCoolerControl(hasCoolerControl)
            .setIsCoolerOn(isCoolerOn)
            .addAllFrameFormats(frameFormats)
            .setCanAbort(canAbort)
            .setCfaOffsetX(cfaOffsetX)
            .setCfaOffsetY(cfaOffsetY)
            .setCfaType(cfaType.name)
            .setExposureMin(exposureMin)
            .setExposureMax(exposureMax)
            .setExposureState(exposureState.name)
            .setHasCooler(hasCooler)
            .setCanSetTemperature(canSetTemperature)
            .setTemperature(temperature)
            .setCanSubframe(canSubframe)
            .setMinX(minX)
            .setMaxX(maxX)
            .setMinY(minY)
            .setMaxY(maxY)
            .setMinWidth(minWidth)
            .setMaxWidth(maxWidth)
            .setMinHeight(minHeight)
            .setMaxHeight(maxHeight)
            .setCanBin(canBin)
            .setMaxBinX(maxBinX)
            .setMaxBinY(maxBinY)
            .build()!!
    }
}
