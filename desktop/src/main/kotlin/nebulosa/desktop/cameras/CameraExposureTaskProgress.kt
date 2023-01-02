package nebulosa.desktop.cameras

import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.protocol.PropertyState
import java.nio.file.Path

data class CameraExposureTaskProgress(
    @JvmField val task: CameraExposureTask,
    @JvmField val progress: Double = 0.0,
    @JvmField val state: PropertyState = PropertyState.IDLE,
    @JvmField val imagePath: Path? = null,
    @JvmField val finishedWithError: Boolean = false,
    @JvmField val isAborted: Boolean = false,
    @JvmField val isFinished: Boolean = false,
    @JvmField val isCapturing: Boolean = false,
    @JvmField val remaining: Int = 0,
) : DeviceEvent<Camera> {

    override val device get() = task.camera
}
