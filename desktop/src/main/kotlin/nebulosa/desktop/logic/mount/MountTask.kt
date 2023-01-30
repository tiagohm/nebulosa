package nebulosa.desktop.logic.mount

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.mount.Mount

sealed interface MountTask : Task<Unit> {

    val mount: Mount
}
