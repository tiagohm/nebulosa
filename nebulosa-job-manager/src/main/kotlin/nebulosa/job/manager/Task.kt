package nebulosa.job.manager

import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.PauseListener

fun interface Task : CancellationListener, PauseListener, Runnable {

    override fun onCancel(source: CancellationSource) = Unit

    override fun onPause(paused: Boolean) = Unit
}
