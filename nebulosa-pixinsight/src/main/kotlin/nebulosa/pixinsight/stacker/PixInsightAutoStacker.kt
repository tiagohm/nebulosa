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
            var stackCount = 0

            paths.forEach {
                var targetPath = it

                if (stacker.calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    targetPath = calibratedPath
                }

                if (stackCount > 0) {
                    if (stacker.align(referencePath, targetPath, alignedPath)) {
                        stacker.integrate(stackCount, outputPath, alignedPath, outputPath)
                        stackCount++
                    }
                } else {
                    targetPath.copyTo(outputPath, true)
                    stackCount = 1
                }
            }
        } finally {
            calibratedPath.deleteIfExists()
            alignedPath.deleteIfExists()
        }

        return true
    }

    override fun calibrate(targetPath: Path, outputPath: Path, darkPath: Path?, flatPath: Path?, biasPath: Path?): Boolean {
        return stacker.calibrate(targetPath, outputPath, darkPath, flatPath, biasPath)
    }

    override fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean {
        return stacker.align(referencePath, targetPath, outputPath)
    }

    override fun integrate(stackCount: Int, stackedPath: Path, targetPath: Path, outputPath: Path): Boolean {
        return stacker.integrate(stackCount, stackedPath, targetPath, outputPath)
    }

    override fun combineLRGB(outputPath: Path, luminancePath: Path?, redPath: Path?, greenPath: Path?, bluePath: Path?): Boolean {
        return stacker.combineLRGB(outputPath, luminancePath, redPath, greenPath, bluePath)
    }

    override fun combineLuminance(outputPath: Path, luminancePath: Path, targetPath: Path, mono: Boolean): Boolean {
        return stacker.combineLuminance(outputPath, luminancePath, targetPath, mono)
    }
}
