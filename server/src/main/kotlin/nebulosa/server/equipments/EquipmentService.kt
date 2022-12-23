package nebulosa.server.equipments

import nebulosa.indi.devices.ConnectionType
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.Camera
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
                require(imagingCamera == null)
                val camera = cameraService.list().firstOrNull { it.name == name }
                require(camera != null)

                return if (!camera.isConnected) {
                    deviceWaitingForConnection = camera
                    camera.connect()

                    if (waitDeviceForConnection() && camera.isConnected) {
                        imagingCamera = camera
                        camera
                    } else {
                        throw IllegalStateException("connection failed")
                    }
                } else {
                    imagingCamera = camera
                    camera
                }
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

    private fun waitDeviceForConnection(): Boolean {
        var counter = 30

        while (counter > 0 && deviceWaitingForConnection != null) {
            Thread.sleep(1000L)
            counter--
        }

        return counter > 0L && deviceWaitingForConnection == null
    }
}
