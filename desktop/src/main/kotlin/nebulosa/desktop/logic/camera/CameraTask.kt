package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.cameras.Camera

interface CameraTask : Task {

    val camera: Camera
}
