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
    @JvmField val dark: Path? = null,
    @JvmField val flat: Path? = null,
    @JvmField val use32Bits: Boolean = false,
) : SirilCommand<Boolean>, CommandLineListener {

    private val command by lazy {
        buildString(256) {
            append("start_ls")
            if (dark != null && dark.exists() && dark.isRegularFile()) append(" \"-dark=$dark\"")
            if (flat != null && flat.exists() && flat.isRegularFile()) append(" \"-flat=$flat\"")
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
