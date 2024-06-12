package nebulosa.siril.command

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLineListener
import nebulosa.siril.command.SirilCommand.Companion.SCRIPT_EXECUTION_FAILED
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sets the new current working [directory].
 */
data class Cd(@JvmField val directory: Path) : SirilCommand<Boolean>, CommandLineListener {

    private val latch = CountUpDownLatch(1)
    private val success = AtomicBoolean()

    override fun onLineRead(line: String) {
        if (line.startsWith(SCRIPT_EXECUTION_FAILED)) {
            success.set(false)
        } else if (line.startsWith("log: Setting CWD")) {
            success.set(true)
        } else {
            return
        }

        latch.reset()
    }

    override fun write(commandLine: SirilCommandLine): Boolean {
        return try {
            commandLine.registerCommandLineListener(this)
            commandLine.write("cd $directory")
            latch.await(30, TimeUnit.SECONDS) && success.get()
        } finally {
            commandLine.unregisterCommandLineListener(this)
        }
    }
}
