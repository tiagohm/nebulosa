package nebulosa.pixinsight.stacker

import nebulosa.pixinsight.script.PixInsightAlign
import nebulosa.pixinsight.script.PixInsightCalibrate
import nebulosa.pixinsight.script.PixInsightLRGBCombination
import nebulosa.pixinsight.script.PixInsightLuminanceCombination
import nebulosa.pixinsight.script.PixInsightPixelMath
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stacker.Stacker
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists

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
            .use { calibrate -> calibrate.runSync(runner).outputImage?.also { it.copyTo(outputPath, true); it.deleteIfExists() } != null }
    } else {
        false
    }

    override fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean {
        return PixInsightAlign(slot, workingDirectory, referencePath, targetPath)
            .use { align -> align.runSync(runner).outputImage?.also { it.copyTo(outputPath, true); it.deleteIfExists() } != null }
    }

    override fun integrate(stackCount: Int, stackedPath: Path, targetPath: Path, outputPath: Path): Boolean {
        val expressionRK = "({{0}} * $stackCount + {{1}}) / ${stackCount + 1}"
        return PixInsightPixelMath(slot, listOf(stackedPath, targetPath), outputPath, expressionRK)
            .use { it.runSync(runner).outputImage != null }
    }

    override fun combineLRGB(outputPath: Path, luminancePath: Path?, redPath: Path?, greenPath: Path?, bluePath: Path?): Boolean {
        if (luminancePath == null && redPath == null && greenPath == null && bluePath == null) return false

        return PixInsightLRGBCombination(slot, outputPath, luminancePath, redPath, greenPath, bluePath)
            .use { it.runSync(runner).outputImage != null }
    }

    override fun combineLuminance(outputPath: Path, luminancePath: Path, targetPath: Path, mono: Boolean): Boolean {
        return if (mono) {
            PixInsightPixelMath(slot, listOf(luminancePath, targetPath), outputPath, "{{0}} + (1 - {{0}}) * {{1}}")
                .use { it.runSync(runner).outputImage != null }
        } else {
            PixInsightLuminanceCombination(slot, outputPath, luminancePath, targetPath)
                .use { it.runSync(runner).outputImage != null }
        }
    }
}
