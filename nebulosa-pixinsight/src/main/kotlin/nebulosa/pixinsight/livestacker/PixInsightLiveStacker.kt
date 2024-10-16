package nebulosa.pixinsight.livestacker

import nebulosa.livestacker.LiveStacker
import nebulosa.log.d
import nebulosa.log.dw
import nebulosa.log.loggerFor
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import nebulosa.pixinsight.stacker.PixInsightStacker
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension

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
    private val calibratedPath = Path.of("$workingDirectory", "calibrated.xisf")
    private val alignedPath = Path.of("$workingDirectory", "aligned.xisf")

    @Volatile private var referencePath: Path? = null
    @Volatile private var stackCount = 0

    override val stacker = PixInsightStacker(runner, workingDirectory, slot)

    override val isRunning
        get() = running.get()

    override val isStacking
        get() = stacking.get()

    override val stackedPath: Path = Path.of("$workingDirectory", "stacked.xisf")

    override fun start() {
        if (running.compareAndSet(false, true)) {
            if (!PixInsightIsRunning(slot).use { it.runSync(runner).success }) {
                try {
                    check(PixInsightStartup(slot).use { it.runSync(runner).success })
                } catch (e: Throwable) {
                    throw IllegalStateException("unable to start PixInsight", e)
                }
            }

            stackCount = 0
        }
    }

    @Synchronized
    override fun add(path: Path, referencePath: Path?): Path? {
        try {
            if (running.get()) {
                stacking.set(true)

                var targetPath = path

                if (stacker.calibrate(targetPath, calibratedPath, darkPath, flatPath, biasPath)) {
                    LOG.d("live stacking calibrated. count={}, target={}, output={}", stackCount, targetPath, calibratedPath)
                    targetPath = calibratedPath
                }

                // TODO: Debayer, Resample?

                if (stackCount > 0) {
                    if (stacker.align(this.referencePath!!, targetPath, alignedPath)) {
                        LOG.d("live stacking aligned. count={}, target={}, output={}", stackCount, targetPath, alignedPath)
                        targetPath = alignedPath

                        if (stacker.integrate(stackCount, stackedPath, targetPath, stackedPath)) {
                            LOG.d("live stacking integrated. count={}, target={}, output={}", stackCount, targetPath, stackedPath)
                            stackCount++
                        }
                    }
                } else {
                    if (this.referencePath == null) {
                        this.referencePath = with(referencePath ?: targetPath) {
                            Path("$workingDirectory", "reference.${extension}").also { copyTo(it, true) }
                        }
                    }

                    if (!stacker.align(this.referencePath!!, targetPath, stackedPath)) {
                        LOG.dw("alignment failed. reference={}, target={}", this.referencePath, targetPath)
                        return null
                    }

                    stackCount = 1
                }

                return stackedPath
            }
        } finally {
            stacking.set(false)
        }

        return null
    }

    override fun stop() {
        running.set(false)
        stackCount = 0
    }

    override fun close() {
        stop()

        referencePath?.deleteIfExists()
        calibratedPath.deleteIfExists()
        alignedPath.deleteIfExists()
        stackedPath.deleteIfExists()
    }

    companion object {

        @JvmStatic val LOG = loggerFor<PixInsightLiveStacker>()
    }
}
