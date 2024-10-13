package nebulosa.pixinsight.script

import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.exec.commandLine
import java.nio.file.Path

data class PixInsightScriptRunner(private val executablePath: Path) {

    fun run(script: PixInsightScript<*>) {
        val commandLine = commandLine {
            executablePath(executablePath)
            script.arguments.forEach(::putArg)
            DEFAULT_ARGS.forEach(::putArg)
        }

        LOG.d("running {} script: {}", script::class.simpleName, commandLine.command)

        script.startCommandLine(commandLine)
    }

    fun abort(script: PixInsightScript<*>) = Unit

    companion object {

        @JvmStatic private val DEFAULT_ARGS = arrayOf("--automation-mode", "--no-startup-scripts")
        @JvmStatic private val LOG = loggerFor<PixInsightScriptRunner>()
    }
}
