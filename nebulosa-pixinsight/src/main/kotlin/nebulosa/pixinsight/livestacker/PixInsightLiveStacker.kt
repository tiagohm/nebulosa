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
    private val referencePath = Path.of("$workingDirectory", "reference.xisf")
    private val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
    private val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

    @Volatile private var stackCount = 0

    override val stacker = PixInsightStacker(runner, workingDirectory, slot)

    override val isRunning
        get() = running.get()

    override val isStacking
        get() = stacking.get()

    override var stackedPath: Path? = null
        private set

    @Synchronized
    override fun start() {
        if (!running.get()) {
            if (!PixInsightIsRunning(slot).use { it.runSync(runner).success }) {
                try {
                    check(PixInsightStartup(slot).use { it.runSync(runner).success })
                } catch (e: Throwable) {
                    throw IllegalStateException("unable to start PixInsight", e)
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

                    if (stacker.integrate(stackCount, stackedPath!!, targetPath, stackedPath!!)) {
                        LOG.info("live stacking finished. count={}, output={}", stackCount, stackedPath)
                    }

                    stackCount++
                }
            } else {
                with(Path.of("$workingDirectory", "stacked.fits")) {
                    if (stacker.saveAs(targetPath, referencePath) && stacker.saveAs(targetPath, this)) {
                        LOG.info("live stacking started. target={}, reference={}, stacked={}", targetPath, referencePath, this)
                        stackCount = 1
                        stackedPath = this
                    }
                }
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
        stackedPath?.deleteIfExists()
    }

    companion object {

        @JvmStatic val LOG = loggerFor<PixInsightLiveStacker>()
    }
}
