package nebulosa.pixinsight.script

import nebulosa.common.exec.commandLine
import nebulosa.log.loggerFor
import java.nio.file.Path

data class PixInsightScriptRunner(private val executablePath: Path) {

    fun run(script: PixInsightScript<*>) {
        val commandLine = commandLine {
            executablePath(executablePath)
            script.arguments.forEach(::putArg)
            DEFAULT_ARGS.forEach(::putArg)
        }

        LOG.info("running PixInsight script: {}", commandLine.command)

        script.startCommandLine(commandLine)
    }

    companion object {

        @JvmStatic private val DEFAULT_ARGS = arrayOf("--automation-mode", "--no-startup-scripts")
        @JvmStatic private val LOG = loggerFor<PixInsightScriptRunner>()
    }
}
