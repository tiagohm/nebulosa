package nebulosa.pixinsight.livestacking

import nebulosa.livestacking.LiveStacker
import nebulosa.log.loggerFor
import nebulosa.pixinsight.script.*
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo

data class PixInsightLiveStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val bias: Path? = null,
    private val use32Bits: Boolean = false,
    private val slot: Int = PixInsightScript.DEFAULT_SLOT,
) : LiveStacker {

    private val running = AtomicBoolean()
    private val stacking = AtomicBoolean()

    override val isRunning
        get() = running.get()

    override val isStacking
        get() = stacking.get()

    @Volatile private var stackCount = 0

    private val referencePath = Path.of("$workingDirectory", "reference.fits")
    private val calibratedPath = Path.of("$workingDirectory", "calibrated.fits")
    private val alignedPath = Path.of("$workingDirectory", "aligned.fits")
    private val stackedPath = Path.of("$workingDirectory", "stacked.fits")

    @Synchronized
    override fun start() {
        if (!running.get()) {
            val isPixInsightRunning = PixInsightIsRunning(slot).use { it.runSync(runner) }

            if (!isPixInsightRunning) {
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

            // Calibrate.
            val calibratedPath = if (dark == null && flat == null && bias == null) null else {
                PixInsightCalibrate(slot, targetPath, dark, flat, if (dark == null) bias else null).use { s ->
                    val outputPath = s.runSync(runner).outputImage ?: return@use null
                    LOG.info("live stacking calibrated. count={}, image={}", stackCount, outputPath)
                    outputPath.moveTo(calibratedPath, true)
                }
            }

            if (calibratedPath != null) {
                targetPath = calibratedPath
            }

            // TODO: Debayer, Resample?

            if (stackCount > 0) {
                // Align.
                val alignedPath = PixInsightAlign(slot, referencePath, targetPath).use { s ->
                    val outputPath = s.runSync(runner).outputImage ?: return@use null
                    LOG.info("live stacking aligned. count={}, image={}", stackCount, alignedPath)
                    outputPath.moveTo(alignedPath, true)
                }

                if (alignedPath != null) {
                    targetPath = alignedPath
                }

                // Stack.
                val expressionRK = "({{0}} * $stackCount + {{1}}) / ${stackCount + 1}"
                PixInsightPixelMath(slot, listOf(stackedPath, targetPath), stackedPath, expressionRK).use { s ->
                    s.runSync(runner).stackedImage?.also {
                        LOG.info("live stacking finished. count={}, image={}", stackCount, it)
                        stackCount++
                    }
                }
            } else {
                targetPath.copyTo(referencePath, true)
                targetPath.copyTo(stackedPath, true)
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
        // stackedPath.deleteIfExists()
    }

    companion object {

        @JvmStatic val LOG = loggerFor<PixInsightLiveStacker>()
    }
}
