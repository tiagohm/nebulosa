package nebulosa.commandline

import nebulosa.log.d
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

data class CommandLine(
    private val commands: Iterable<String?>,
    private val workingDirectory: Path? = null,
    private val environment: Map<String, String> = emptyMap(),
) {

    @Volatile private var process: Process? = null

    fun execute(handler: CommandLineHandler? = null, timeout: Long = 0L, unit: TimeUnit = TimeUnit.MILLISECONDS): CommandLineResult {
        val commands = commands.filter { !it.isNullOrEmpty() }.toTypedArray()

        with(ProcessBuilder(*commands).directory(workingDirectory?.toFile()).also { it.environment().putAll(environment) }.start()) {
            LOG.d { info("executing command: {}. pid={}", commands, pid()) }

            process = this

            handler?.setProcessErrorStream(errorStream)
            handler?.setProcessOutputStream(inputStream)
            handler?.setProcessInputStream(outputStream)
            handler?.start(this)

            try {
                if (timeout > 0L) {
                    if (!waitFor(timeout, unit)) {
                        val exception = TimeoutException("the command took more than $timeout $unit to execute")
                        handler?.onProcessFailed(exception)
                        return CommandLineResult(Int.MIN_VALUE, exception)
                    }
                } else {
                    waitFor()
                }

                val exitCode = exitValue()
                handler?.onProcessComplete(exitCode)
                return CommandLineResult(exitCode, null)
            } catch (e: Throwable) {
                if (e is InterruptedException) {
                    Thread.currentThread().interrupt()
                }

                handler?.onProcessFailed(e)

                if (isAlive) {
                    destroyForcibly()
                    waitFor()
                }

                return CommandLineResult(exitValue(), e)
            } finally {
                handler?.stop()

                if (isAlive) {
                    destroyForcibly()
                }
            }
        }
    }

    companion object {

        private val LOG = loggerFor<CommandLine>()
    }
}
