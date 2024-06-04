package nebulosa.pixinsight.livestacking

import nebulosa.livestacking.LiveStacker
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

data class PixInsightLiveStacker(
    private val runner: PixInsightScriptRunner,
    private val workingDirectory: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val bias: Path? = null,
    private val use32Bits: Boolean = false,
) : LiveStacker {

    private val running = AtomicBoolean()

    override val isRunning
        get() = running.get()

    override val isStacking: Boolean
        get() = TODO("Not yet implemented")

    @Synchronized
    override fun start() {
        val isPixInsightRunning = PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT).runSync(runner)

        if (!isPixInsightRunning) {
            try {
                check(PixInsightStartup(PixInsightScript.DEFAULT_SLOT).runSync(runner))
            } catch (e: Throwable) {
                throw IllegalStateException("Unable to start PixInsight")
            }

            running.set(true)
        }
    }

    @Synchronized
    override fun add(path: Path): Path? {
        return null
    }

    @Synchronized
    override fun stop() {

    }

    override fun close() {

    }
}
