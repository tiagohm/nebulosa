package nebulosa.pixinsight.stacker

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stacker.AutoStacker
import nebulosa.stacker.AutoStackerListener
import java.nio.file.Path
import java.util.concurrent.CancellationException
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
    private val listeners = HashSet<AutoStackerListener>()

    override fun registerAutoStackerListener(listener: AutoStackerListener) {
        listeners.add(listener)
    }

    override fun unregisterAutoStackerListener(listener: AutoStackerListener) {
        listeners.remove(listener)
    }

    override fun stack(targetPaths: Collection<Path>, outputPath: Path, referencePath: Path, cancellationToken: CancellationToken): Boolean {
        if (targetPaths.isEmpty()) return false

        val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
        val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

        try {
            var stackCount = 0

            for (path in targetPaths) {
                var targetPath = path

                if (cancellationToken.isCancelled) return false

                listeners.forEach { it.onCalibrated(stackCount, path, calibratedPath) }

                if (calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    targetPath = calibratedPath
                }

                if (cancellationToken.isCancelled) return false

                if (stackCount > 0) {
                    listeners.forEach { it.onAligned(stackCount, path, alignedPath) }

                    if (align(referencePath, targetPath, alignedPath)) {
                        if (cancellationToken.isCancelled) return false
                        listeners.forEach { it.onIntegrated(stackCount, path, outputPath) }
                        integrate(stackCount, outputPath, alignedPath, outputPath)
                        stackCount++
                    }
                } else {
                    if (referencePath != path) {
                        listeners.forEach { it.onAligned(stackCount, path, alignedPath) }

                        if (align(referencePath, targetPath, alignedPath)) {
                            if (cancellationToken.isCancelled) return false
                            saveAs(alignedPath, outputPath)
                            if (cancellationToken.isCancelled) return false
                            listeners.forEach { it.onIntegrated(0, path, outputPath) }
                            integrate(0, outputPath, alignedPath, outputPath)
                        } else {
                            saveAs(targetPath, outputPath)
                        }
                    } else {
                        saveAs(targetPath, outputPath)
                    }

                    stackCount = 1
                }

                if (cancellationToken.isCancelled) return false
            }
        } catch (_: CancellationException) {
            return false
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

    override fun saveAs(inputPath: Path, outputPath: Path): Boolean {
        return stacker.saveAs(inputPath, outputPath)
    }
}
