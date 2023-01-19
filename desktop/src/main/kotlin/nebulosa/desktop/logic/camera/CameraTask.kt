package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.taskexecutor.Task
import nebulosa.indi.device.cameras.Camera

interface CameraTask : Task {

    val camera: Camera
}
