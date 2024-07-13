package nebulosa.pixinsight.livestacker

import nebulosa.livestacker.LiveStacker
import nebulosa.log.loggerFor
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import nebulosa.pixinsight.stacker.PixInsightStacker
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists

data class PixInsightLiveStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val biasPath: Path? = null,
    private val use32Bits: Boolean = false,
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : LiveStacker {

    private val running = AtomicBoolean()
    private val stacking = AtomicBoolean()

    override val isRunning
        get() = running.get()

    override val isStacking
        get() = stacking.get()

    @Volatile private var stackCount = 0

    private val stacker = PixInsightStacker(runner, workingDirectory, slot)
    private val referencePath = Path.of("$workingDirectory", "reference.fits")
    private val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
    private val alignedPath = Path.of("$workingDirectory", "aligned.xisf")
    private val stackedPath = Path.of("$workingDirectory", "stacked.fits")

    @Synchronized
    override fun start() {
        if (!running.get()) {
            if (!PixInsightIsRunning(slot).use { it.runSync(runner) }) {
                try {
                    check(PixInsightStartup(slot).use { it.runSync(runner) })
                } catch (e: Throwable) {
                    throw IllegalStateException("unable to start PixInsight")
                }
            }

            stackCount = 0
            running.set(true)
        }
    }

    @Synchronized
    override fun add(path: Path): Path? {
        var targetPath = path

        return if (running.get()) {
            stacking.set(true)

            if (stacker.calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                LOG.info("live stacking calibrated. count={}, output={}", stackCount, calibratedPath)
                targetPath = calibratedPath
            }

            // TODO: Debayer, Resample?

            if (stackCount > 0) {
                if (stacker.align(referencePath, targetPath, alignedPath)) {
                    LOG.info("live stacking aligned. count={}, output={}", stackCount, alignedPath)
                    targetPath = alignedPath

                    if (stacker.integrate(stackCount, stackedPath, targetPath, stackedPath)) {
                        LOG.info("live stacking finished. count={}, output={}", stackCount, stackedPath)
                    }

                    stackCount++
                }
            } else {
                targetPath.copyTo(referencePath, true)
                targetPath.copyTo(stackedPath, true)
                LOG.info("live stacking started. target={}, reference={}, stacked={}", targetPath, referencePath, stackedPath)
                stackCount = 1
            }

            stacking.set(false)

            stackedPath
        } else {
            path
        }
    }

    @Synchronized
    override fun stop() {
        running.set(false)
        stackCount = 0
    }

    override fun close() {
        referencePath.deleteIfExists()
        calibratedPath.deleteIfExists()
        alignedPath.deleteIfExists()
        stackedPath.deleteIfExists()
    }

    companion object {

        @JvmStatic val LOG = loggerFor<PixInsightLiveStacker>()
    }
}
