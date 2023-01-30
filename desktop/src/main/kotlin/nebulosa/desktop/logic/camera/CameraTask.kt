package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

sealed interface CameraTask : Task<List<Path>> {

    val camera: Camera
}
