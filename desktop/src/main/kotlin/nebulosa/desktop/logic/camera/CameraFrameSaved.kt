package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.task.TaskEvent
import java.nio.file.Path

data class CameraFrameSaved(
    override val task: CameraExposureTask,
    val imagePath: Path,
    val autoSave: Boolean = false,
) : TaskEvent
