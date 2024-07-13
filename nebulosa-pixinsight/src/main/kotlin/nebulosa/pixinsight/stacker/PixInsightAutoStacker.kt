package nebulosa.pixinsight.stacker

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stacker.AutoStacker
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.deleteIfExists

data class PixInsightAutoStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val biasPath: Path? = null,
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : AutoStacker {

    private val cancellationToken = AtomicReference<CancellationToken>()
    private val stacker = PixInsightStacker(runner, workingDirectory, slot)

    override fun stack(paths: Collection<Path>, outputPath: Path, referencePath: Path): Boolean {
        if (paths.isEmpty()) return false
        if (!cancellationToken.compareAndSet(null, CancellationToken())) return false

        val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
        val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

        try {
            var stackCount = 0

            val realPaths = paths.map { it.toRealPath() }
            val referenceRealPath = referencePath.toRealPath()

            realPaths.forEach {
                var targetPath = it

                cancellationToken.get().throwIfCancelled()

                if (calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    targetPath = calibratedPath
                }

                cancellationToken.get().throwIfCancelled()

                if (stackCount > 0) {
                    if (align(referenceRealPath, targetPath, alignedPath)) {
                        cancellationToken.get().throwIfCancelled()
                        integrate(stackCount, outputPath, alignedPath, outputPath)
                        stackCount++
                    }
                } else {
                    if (referenceRealPath != it) {
                        if (align(referenceRealPath, targetPath, alignedPath)) {
                            cancellationToken.get().throwIfCancelled()
                            saveAs(alignedPath, outputPath)
                            cancellationToken.get().throwIfCancelled()
                            integrate(0, outputPath, alignedPath, outputPath)
                        } else {
                            saveAs(targetPath, outputPath)
                        }
                    } else {
                        saveAs(targetPath, outputPath)
                    }

                    stackCount = 1
                }

                cancellationToken.get().throwIfCancelled()
            }
        } catch (e: CancellationException) {
            return false
        } finally {
            calibratedPath.deleteIfExists()
            alignedPath.deleteIfExists()

            cancellationToken.getAndSet(null)
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

    override fun stop() {
        cancellationToken.get()?.cancel()
    }
}
