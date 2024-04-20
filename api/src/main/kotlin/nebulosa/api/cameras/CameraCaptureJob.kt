package nebulosa.api.cameras

import nebulosa.common.concurrency.cancel.CancellationToken

data class CameraCaptureJob(
    @JvmField val task: CameraCaptureTask,
    @JvmField val cancellationToken: CancellationToken,
) : Thread("${task.camera.name} Camera Capture Job") {

    init {
        isDaemon = false
    }

    override fun run() {
        task.execute(cancellationToken)
    }

    fun abort() {
        cancellationToken.cancel()
        task.close()
    }
}
