package nebulosa.pixinsight.script

import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.concurrent.CompletableFuture

data class PixInsightIsRunning(override val slot: Int) : AbstractPixInsightScript<PixInsightIsRunning.Output>() {

    data class Output(
        override val success: Boolean,
        override val errorMessage: String? = null,
    ) : PixInsightScriptOutput {

        companion object {

            @JvmStatic val SUCCESS = Output(true)
            @JvmStatic val FAILED = Output(false)
        }
    }

    override val arguments = listOf(if (slot > 0) "-y=$slot" else "-y")

    private val slotIsNotRunning = "The requested application instance #$slot is not running"
    private val slotCrashed = "The requested application instance #$slot has crashed"
    private val yieldedExecutionInstance = "$YIELDED_EXECUTION_INSTANCE$slot"

    override fun processLine(line: String, output: CompletableFuture<Output>) {
        if (output.isDone) return

        if (slot > 0) {
            if (line.contains(slotIsNotRunning, true) || line.contains(slotCrashed, true)) {
                output.complete(Output.FAILED)
            } else if (line.contains(yieldedExecutionInstance, true)) {
                output.complete(Output.SUCCESS)
            } else {
                return
            }
        } else if (line.contains(YIELDED_EXECUTION_INSTANCE, true)) {
            output.complete(Output.SUCCESS)
        } else if (line.contains(NO_RUNNING_PROCESS, true)) {
            output.complete(Output.FAILED)
        } else {
            return
        }

        LOG.d(line)
    }

    override fun processOnExit(exitCode: Int, output: CompletableFuture<Output>) {
        if (exitCode != 0) output.complete(Output.FAILED)
    }

    override fun close() = Unit

    companion object {

        private const val NO_RUNNING_PROCESS = "There is no running (and alive) instance of the PixInsight"
        private const val YIELDED_EXECUTION_INSTANCE = "Yielded execution to running application instance #"

        @JvmStatic private val LOG = loggerFor<PixInsightIsRunning>()
    }
}
