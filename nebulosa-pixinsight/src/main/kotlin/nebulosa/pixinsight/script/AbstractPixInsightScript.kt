package nebulosa.pixinsight.script

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import nebulosa.json.PathModule
import nebulosa.log.loggerFor
import nebulosa.util.exec.CommandLine
import nebulosa.util.exec.CommandLineListener
import org.apache.commons.codec.binary.Hex
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.readText

abstract class AbstractPixInsightScript<T : PixInsightScript.Output> : PixInsightScript<T>, CommandLineListener, CompletableFuture<T>() {

    override fun onLineRead(line: String) = Unit

    override fun onExit(exitCode: Int, exception: Throwable?) = Unit

    protected open fun beforeRun() = Unit

    protected abstract fun processOnComplete(exitCode: Int): T?

    protected open fun waitOnComplete() = Unit

    final override fun run(runner: PixInsightScriptRunner) = runner.run(this)

    final override fun startCommandLine(commandLine: CommandLine) {
        commandLine.whenComplete { exitCode, exception ->
            try {
                LOG.info("{} script finished. done={}, exitCode={}", this::class.simpleName, isDone, exitCode, exception)

                waitOnComplete()

                if (isDone) return@whenComplete
                else if (exception != null) completeExceptionally(exception)
                else complete(processOnComplete(exitCode).also { LOG.info("{} script processed. output={}", this::class.simpleName, it) })
            } catch (e: Throwable) {
                LOG.error("{} finished with fatal exception. message={}", this::class.simpleName, e.message)
                completeExceptionally(e)
            } finally {
                commandLine.unregisterCommandLineListener(this)
            }
        }

        commandLine.registerCommandLineListener(this)
        beforeRun()
        commandLine.start()
    }

    companion object {

        internal const val START_FILE = "@"
        internal const val END_FILE = "#"

        @JvmStatic private val LOG = loggerFor<AbstractPixInsightScript<*>>()

        @JvmStatic internal val OBJECT_MAPPER = jsonMapper {
            addModule(PathModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

        @JvmStatic
        internal fun PixInsightScript<*>.execute(scriptPath: Path, data: Any?, slot: Int = this.slot): String {
            LOG.info("{} will be executed. slot={}, script={}, data={}", this::class.simpleName, slot, scriptPath, data)

            return buildString {
                if (slot > 0) append("$slot:")
                append("\"$scriptPath")

                if (data != null) {
                    append(',')

                    when (data) {
                        is Path, is CharSequence -> append("'$data'")
                        is Number -> append("$data")
                        else -> append(Hex.encodeHexString(OBJECT_MAPPER.writeValueAsString(data).toByteArray(Charsets.UTF_16BE)))
                    }
                }

                append('"')
            }
        }

        @JvmStatic
        internal fun <T : PixInsightScript.Output> Path.parseStatus(type: Class<T>): T? {
            val text = readText()

            return if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), type)
            } else {
                null
            }
        }

        internal inline fun <reified T : PixInsightScript.Output> Path.parseStatus(): T? {
            return parseStatus(T::class.java)
        }
    }
}
