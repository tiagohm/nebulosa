package nebulosa.pixinsight.stacker

import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stacker.AutoStacker
import nebulosa.stacker.AutoStackerListener
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
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
    private val stopped = AtomicBoolean()

    override fun registerAutoStackerListener(listener: AutoStackerListener) {
        listeners.add(listener)
    }

    override fun unregisterAutoStackerListener(listener: AutoStackerListener) {
        listeners.remove(listener)
    }

    override fun stack(targetPaths: Collection<Path>, outputPath: Path, referencePath: Path): Boolean {
        if (targetPaths.isEmpty()) return false

        val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
        val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

        stopped.set(false)

        try {
            var stackCount = 0

            for (path in targetPaths) {
                var targetPath = path

                if (stopped.get()) return false

                listeners.forEach { it.onCalibrationStarted(stackCount, path) }

                if (calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    listeners.forEach { it.onCalibrationFinished(stackCount, path, calibratedPath) }
                    targetPath = calibratedPath
                }

                if (stopped.get()) return false

                if (stackCount > 0) {
                    listeners.forEach { it.onAlignStarted(stackCount, path) }

                    if (align(referencePath, targetPath, alignedPath)) {
                        listeners.forEach { it.onAlignFinished(stackCount, path, alignedPath) }

                        if (stopped.get()) return false

                        listeners.forEach { it.onIntegrationStarted(stackCount, path) }
                        integrate(stackCount, outputPath, alignedPath, outputPath)
                        listeners.forEach { it.onIntegrationFinished(stackCount, path, outputPath) }
                        stackCount++
                    }
                } else {
                    if (referencePath != path) {
                        listeners.forEach { it.onAlignStarted(stackCount, path) }

                        if (align(referencePath, targetPath, alignedPath)) {
                            listeners.forEach { it.onAlignFinished(stackCount, path, alignedPath) }

                            if (stopped.get()) return false

                            saveAs(alignedPath, outputPath)

                            if (stopped.get()) return false

                            listeners.forEach { it.onIntegrationStarted(0, path) }
                            integrate(0, outputPath, alignedPath, outputPath)
                            listeners.forEach { it.onIntegrationFinished(0, path, outputPath) }
                        } else {
                            saveAs(targetPath, outputPath)
                        }
                    } else {
                        saveAs(targetPath, outputPath)
                    }

                    stackCount = 1
                }

                if (stopped.get()) return false
            }
        } catch (_: CancellationException) {
            return false
        } finally {
            calibratedPath.deleteIfExists()
            alignedPath.deleteIfExists()
        }

        return true
    }

    override fun stop() {
        stopped.set(true)
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
