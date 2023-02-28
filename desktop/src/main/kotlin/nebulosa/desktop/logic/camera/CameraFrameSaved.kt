package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.imaging.Image
import java.nio.file.Path

data class CameraFrameSaved(
    override val task: CameraExposureTask,
    val image: Image,
    val path: Path,
    val autoSave: Boolean = false,
) : TaskEvent
