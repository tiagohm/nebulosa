package nebulosa.pixinsight.script

import java.nio.file.Path
import kotlin.math.max

fun startPixInsight(executablePath: Path, slot: Int): PixInsightScriptRunner {
    val runner = PixInsightScriptRunner(executablePath)

    if (!PixInsightIsRunning(slot).use { it.runSync(runner).success }) {
        if (!PixInsightStartup(max(1, slot)).use { it.runSync(runner).success }) {
            throw IllegalStateException("unable to start PixInsight")
        }
    }

    return runner
}
