package nebulosa.siril.command

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLineListener
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Processes the provided [path] for live stacking.
 */
data class LiveStack(@JvmField val path: Path) : SirilCommand<Boolean>, CommandLineListener {

    private val latch = CountUpDownLatch(1)
    private val success = AtomicBoolean()

    override fun onLineRead(line: String) {
        if (SUCCESSFUL_LOGS.any { line.startsWith(it, true) }) {
            success.set(true)
        } else if (FAILED_LOGS.any { line.contains(it, true) }) {
            success.set(false)
        } else {
            return
        }

        latch.reset()
    }

    override fun onExit(exitCode: Int, exception: Throwable?) {
        latch.reset()
    }

    override fun write(commandLine: SirilCommandLine): Boolean {
        return try {
            commandLine.registerCommandLineListener(this)
            commandLine.write("livestack \"$path\"")
            latch.await(60, TimeUnit.SECONDS)
        } finally {
            commandLine.unregisterCommandLineListener(this)
        }
    }

    companion object {

        @JvmStatic private val SUCCESSFUL_LOGS = arrayOf(
            "log: Waiting for second image",
            "log: Stacked image",
            "log: Live stacking waiting for files"
        )

        @JvmStatic private val FAILED_LOGS = arrayOf(
            "Not enough stars",
            "Sequence processing partially succeeded",
            "Script execution failed",
        )
    }
}
