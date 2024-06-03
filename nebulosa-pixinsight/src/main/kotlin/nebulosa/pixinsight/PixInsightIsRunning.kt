package nebulosa.pixinsight

import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.util.concurrent.atomic.AtomicBoolean

data class PixInsightIsRunning(private val slot: Int = 0) : PixInsightScript<Boolean> {

    override val arguments = listOf(if (slot > 0) "-y=$slot" else "-y")

    private val completed = AtomicBoolean()
    private val running = AtomicBoolean()

    private val slotIsNotRunning = "The requested application instance #$slot is not running"
    private val slotCrashed = "The requested application instance #$slot has crashed"
    private val yieldedExecutionInstance = "$YIELDED_EXECUTION_INSTANCE$slot"

    override fun onInputRead(line: String) {
        processLine(line)
    }

    override fun onErrorRead(line: String) {
        processLine(line)
    }

    private fun processLine(line: String) {
        if (completed.get()) return

        if (slot > 0) {
            if (line.contains(slotIsNotRunning, true) || line.contains(slotCrashed, true)) {
                completeWith(false)
            } else if (line.contains(yieldedExecutionInstance, true)) {
                completeWith(true)
            } else {
                return
            }
        } else if (line.contains(YIELDED_EXECUTION_INSTANCE, true)) {
            completeWith(true)
        } else if (line.contains(NO_RUNNING_PROCESS, true)) {
            completeWith(false)
        } else {
            return
        }

        LOG.debug { line }
    }

    private fun completeWith(running: Boolean) {
        this.running.set(running)
        completed.set(true)
    }

    override fun processOnComplete(pid: Long, exitCode: Int): Boolean {
        return exitCode == 0 && completed.get() && running.get()
    }

    companion object {

        private const val NO_RUNNING_PROCESS = "There is no running (and alive) instance of the PixInsight"
        private const val YIELDED_EXECUTION_INSTANCE = "Yielded execution to running application instance #"

        @JvmStatic private val LOG = loggerFor<PixInsightIsRunning>()
    }
}
