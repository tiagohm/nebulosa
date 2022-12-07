package nebulosa.api.cameras

import nebulosa.indi.devices.events.CameraEvent
import java.nio.file.Path

data class CameraCaptureSavedEvent(
    val task: CameraCaptureTask,
    val path: Path,
    val isTemporary: Boolean = false,
) : CameraEvent {

    override val device get() = task.camera
}
