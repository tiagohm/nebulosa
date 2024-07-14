package nebulosa.pixinsight.script

import java.nio.file.Path

fun startPixInsight(executablePath: Path, slot: Int): PixInsightScriptRunner {
    val runner = PixInsightScriptRunner(executablePath)

    if (!PixInsightIsRunning(slot).use { it.runSync(runner).success }) {
        if (!PixInsightStartup(slot).use { it.runSync(runner).success }) {
            throw IllegalStateException("unable to start PixInsight")
        }
    }

    return runner
}
