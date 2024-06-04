package nebulosa.pixinsight.script

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import nebulosa.common.json.PathDeserializer
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

abstract class AbstractPixInsightScript<T> : PixInsightScript<T>, LineReadListener, CompletableFuture<T>() {

    override fun onInputRead(line: String) = Unit

    override fun onErrorRead(line: String) = Unit

    protected open fun beforeRun() = Unit

    protected abstract fun processOnComplete(exitCode: Int): T?

    final override fun run(runner: PixInsightScriptRunner) = runner.run(this)

    final override fun handleCommandLine(commandLine: CommandLine) {
        commandLine.whenComplete { exitCode, exception ->
            try {
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

        @JvmStatic private val KOTLIN_MODULE = kotlinModule()
            .addDeserializer(Path::class.java, PathDeserializer)

        @JvmStatic internal val OBJECT_MAPPER = jsonMapper {
            addModule(KOTLIN_MODULE)
        }
    }
}
