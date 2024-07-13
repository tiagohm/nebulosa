package nebulosa.pixinsight.script

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.CommandLineListener
import nebulosa.common.json.PathDeserializer
import nebulosa.common.json.PathSerializer
import nebulosa.log.loggerFor
import org.apache.commons.codec.binary.Hex
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

abstract class AbstractPixInsightScript<T> : PixInsightScript<T>, CommandLineListener, CompletableFuture<T>() {

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

        @JvmStatic private val KOTLIN_MODULE = kotlinModule()
            .addDeserializer(Path::class.java, PathDeserializer)
            .addSerializer(PathSerializer)

        @JvmStatic internal val OBJECT_MAPPER = jsonMapper {
            addModule(KOTLIN_MODULE)
        }

        @JvmStatic
        internal fun PixInsightScript<*>.execute(slot: Int, scriptPath: Path, data: Any?): String {
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
    }
}
