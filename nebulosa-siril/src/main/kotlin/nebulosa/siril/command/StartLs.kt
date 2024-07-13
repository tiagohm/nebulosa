package nebulosa.siril.command

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLineListener
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

/**
 * Initializes a livestacking session.
 */
data class StartLs(
    @JvmField val darkPath: Path? = null,
    @JvmField val flatPath: Path? = null,
    @JvmField val use32Bits: Boolean = false,
) : SirilCommand<Boolean>, CommandLineListener {

    private val command by lazy {
        buildString(256) {
            append("start_ls")
            if (darkPath != null && darkPath.exists() && darkPath.isRegularFile()) append(" \"-dark=$darkPath\"")
            if (flatPath != null && flatPath.exists() && flatPath.isRegularFile()) append(" \"-flat=$flatPath\"")
            if (use32Bits) append(" -32bits")
        }
    }

    private val latch = CountUpDownLatch(1)
    private val success = AtomicBoolean()

    override fun onLineRead(line: String) {
        if (line.startsWith("log: Live stacking waiting for files")) {
            success.set(true)
        } else if (line.startsWith(SirilCommand.SCRIPT_EXECUTION_FAILED)) {
            success.set(false)
        } else {
            return
        }

        latch.reset()
    }

    override fun write(commandLine: SirilCommandLine): Boolean {
        return try {
            commandLine.registerCommandLineListener(this)
            commandLine.write(command)
            latch.await(30, TimeUnit.SECONDS) && success.get()
        } finally {
            commandLine.unregisterCommandLineListener(this)
        }
    }
}
