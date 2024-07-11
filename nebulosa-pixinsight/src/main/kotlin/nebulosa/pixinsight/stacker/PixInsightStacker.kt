package nebulosa.pixinsight.stacker

import nebulosa.pixinsight.script.*
import nebulosa.stacker.Stacker
import java.nio.file.Path
import kotlin.io.path.moveTo

data class PixInsightStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : Stacker {

    override fun calibrate(
        targetPath: Path, outputPath: Path,
        darkPath: Path?, flatPath: Path?, biasPath: Path?,
    ) = if (darkPath != null || flatPath != null || biasPath != null) {
        PixInsightCalibrate(slot, workingDirectory, targetPath, darkPath, flatPath, if (darkPath == null) biasPath else null)
            .use { it.runSync(runner).outputImage?.moveTo(outputPath, true) != null }
    } else {
        false
    }

    override fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean {
        return PixInsightAlign(slot, workingDirectory, referencePath, targetPath)
            .use { it.runSync(runner).outputImage?.moveTo(outputPath, true) != null }
    }

    override fun integrate(stackCount: Int, stackedPath: Path, targetPath: Path, outputPath: Path): Boolean {
        val expressionRK = "({{0}} * $stackCount + {{1}}) / ${stackCount + 1}"
        return PixInsightPixelMath(slot, listOf(stackedPath, targetPath), outputPath, expressionRK)
            .use { it.runSync(runner).stackedImage != null }
    }
}
