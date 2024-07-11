package nebulosa.pixinsight.stacker

import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stacker.AutoStacker
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists

data class PixInsightAutoStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val biasPath: Path? = null,
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : AutoStacker {

    private val stacker = PixInsightStacker(runner, workingDirectory, slot)

    override fun stack(paths: Collection<Path>, outputPath: Path, referencePath: Path): Boolean {
        if (paths.isEmpty()) return false

        val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
        val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

        try {
            paths.forEachIndexed { stackCount, path ->
                var targetPath = path

                if (stacker.calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    targetPath = calibratedPath
                }

                if (stackCount > 0) {
                    if (stacker.align(referencePath, targetPath, alignedPath)) {
                        stacker.integrate(stackCount, outputPath, alignedPath, outputPath)
                    }
                } else {
                    targetPath.copyTo(outputPath, true)
                }
            }
        } finally {
            calibratedPath.deleteIfExists()
            alignedPath.deleteIfExists()
        }

        return true
    }
}
