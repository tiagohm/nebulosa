package nebulosa.job.manager

import nebulosa.util.concurrency.cancellation.CancellationSource

interface JobTask : Job, Task {

    override fun pause() {
        onPause(true)
    }

    override fun unpause() {
        onPause(false)
    }

    override fun stop() {
        onCancel(CancellationSource.DEFAULT)
    }
}
