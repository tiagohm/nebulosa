package nebulosa.pixinsight.script

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import nebulosa.common.json.PathDeserializer
import nebulosa.common.json.PathSerializer
import nebulosa.log.loggerFor
import org.apache.commons.codec.binary.Hex
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

abstract class AbstractPixInsightScript<T> : PixInsightScript<T>, LineReadListener, CompletableFuture<T>() {

    override fun onInputRead(line: String) = Unit

    override fun onErrorRead(line: String) = Unit

    protected open fun beforeRun() = Unit

    protected abstract fun processOnComplete(exitCode: Int): T?

    final override fun run(runner: PixInsightScriptRunner) = runner.run(this)

    final override fun startCommandLine(commandLine: CommandLine) {
        commandLine.whenComplete { exitCode, exception ->
            try {
                LOG.info("PixInsight script finished. done={}, exitCode={}", isDone, exitCode, exception)

                if (isDone) return@whenComplete
                else if (exception != null) completeExceptionally(exception)
                else complete(processOnComplete(exitCode))
            } finally {
                commandLine.unregisterLineReadListener(this)
            }
        }

        commandLine.registerLineReadListener(this)
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
        internal fun execute(slot: Int, scriptPath: Path, data: Any): String {
            return buildString {
                if (slot > 0) append("$slot:")
                append("\"$scriptPath,")
                append(Hex.encodeHexString(OBJECT_MAPPER.writeValueAsBytes(data)))
                append('"')
            }
        }
    }
}
