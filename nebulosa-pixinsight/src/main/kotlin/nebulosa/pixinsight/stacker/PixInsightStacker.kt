package nebulosa.pixinsight.stacker

import nebulosa.pixinsight.script.*
import nebulosa.stacker.Stacker
import java.nio.file.Path

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
            .use { calibrate -> calibrate.runSync(runner).outputImage?.let { saveAs(it, outputPath) } ?: false }
    } else {
        false
    }

    override fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean {
        return PixInsightAlign(slot, workingDirectory, referencePath, targetPath)
            .use { align -> align.runSync(runner).outputImage?.let { saveAs(it, outputPath) } ?: false }
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

    override fun saveAs(inputPath: Path, outputPath: Path): Boolean {
        return PixInsightFileFormatConversion(slot, inputPath, outputPath)
            .use { it.runSync(runner).outputImage != null }
    }
}
