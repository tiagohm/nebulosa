package nebulosa.pixinsight.livestacking

import nebulosa.livestacking.LiveStacker
import nebulosa.pixinsight.script.*
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.moveTo
import kotlin.io.path.name

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
    @Volatile private var referencePath: Path? = null
    @Volatile private var stackedPath: Path? = null

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

                running.set(true)
            }
        }
    }

    @Synchronized
    override fun add(path: Path): Path? {
        var targetPath = path

        if (running.get()) {
            stacking.set(true)

            // Calibrate.
            val calibratedPath = if (dark == null && flat == null && bias == null) null else {
                PixInsightCalibrate(slot, targetPath, dark, flat, if (dark == null) bias else null).use {
                    val outputPath = it.runSync(runner).outputImage ?: return@use null
                    val destinationPath = Path.of("$workingDirectory", outputPath.name)
                    outputPath.moveTo(destinationPath, true)
                }
            }

            if (calibratedPath != null) {
                targetPath = calibratedPath
            }

            // TODO: Debayer, Resample?

            if (stackCount > 0) {
                // Align.
                val alignedPath = PixInsightAlign(slot, referencePath!!, targetPath).use {
                    val outputPath = it.runSync(runner).outputImage ?: return@use null
                    val destinationPath = Path.of("$workingDirectory", outputPath.name)
                    outputPath.moveTo(destinationPath, true)
                }

                if (alignedPath != null) {
                    targetPath = alignedPath
                }

                // Stack.
            } else {
                referencePath = targetPath
            }

            stackedPath = targetPath
            stackCount++

            stacking.set(false)
        }

        return stackedPath
    }

    @Synchronized
    override fun stop() {
        running.set(false)
        stackCount = 0
        referencePath = null
        stackedPath = null
    }

    override fun close() = Unit
}
