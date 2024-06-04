package nebulosa.pixinsight.script

import nebulosa.log.debug
import nebulosa.log.loggerFor

data class PixInsightIsRunning(private val slot: Int) : AbstractPixInsightScript<Boolean>() {

    override val arguments = listOf(if (slot > 0) "-y=$slot" else "-y")

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
        if (isDone) return

        if (slot > 0) {
            if (line.contains(slotIsNotRunning, true) || line.contains(slotCrashed, true)) {
                complete(false)
            } else if (line.contains(yieldedExecutionInstance, true)) {
                complete(true)
            } else {
                return
            }
        } else if (line.contains(YIELDED_EXECUTION_INSTANCE, true)) {
            complete(true)
        } else if (line.contains(NO_RUNNING_PROCESS, true)) {
            complete(false)
        } else {
            return
        }

        LOG.debug { line }
    }

    override fun processOnComplete(exitCode: Int) = false

    override fun close() = Unit

    companion object {

        private const val NO_RUNNING_PROCESS = "There is no running (and alive) instance of the PixInsight"
        private const val YIELDED_EXECUTION_INSTANCE = "Yielded execution to running application instance #"

        @JvmStatic private val LOG = loggerFor<PixInsightIsRunning>()
    }
}
