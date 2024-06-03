package nebulosa.siril.livestacking

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import nebulosa.common.exec.commandLine
import nebulosa.livestacking.LiveStacker
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

data class SirilLiveStacker(
    private val executablePath: Path,
    private val workingDirectory: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val rotate: Angle = 0.0,
    private val use32Bits: Boolean = false,
) : LiveStacker, LineReadListener {

    @Volatile private var commandLine: CommandLine? = null

    private val waitForStacking = CountUpDownLatch()
    private val failed = AtomicBoolean()

    override val isRunning
        get() = commandLine != null && !commandLine!!.isDone

    override val isStacking
        get() = !waitForStacking.get()

    @Synchronized
    override fun start() {
        if (commandLine == null) {
            commandLine = commandLine {
                executablePath(executablePath)
                putArg("-s", "-")
                registerLineReadListener(this@SirilLiveStacker)
            }

            commandLine!!.whenComplete { exitCode, e ->
                LOG.info("live stacking finished. exitCode={}", exitCode, e)
                commandLine = null
            }

            commandLine!!.start()

            LOG.info("live stacking started. pid={}", commandLine!!.pid)

            commandLine!!.writer.println(REQUIRES_COMMAND)
            commandLine!!.writer.println("$CD_COMMAND $workingDirectory")
            commandLine!!.writer.println(buildString(256) {
                append(START_LS_COMMAND)
                if (dark != null) append(" \"-dark=$dark\"")
                if (flat != null) append(" \"-flat=$flat\"")
                if (rotate != 0.0) append(" -rotate=$rotate")
                if (use32Bits) append(" -32bits")
            })
        }
    }

    @Synchronized
    override fun add(path: Path): Path? {
        failed.set(false)

        try {
            waitForStacking.countUp()
            commandLine?.writer?.println("$LS_COMMAND \"$path\"")
            waitForStacking.await()
        } catch (e: Throwable) {
            LOG.error("failed to add path", e)
            return null
        }

        return if (failed.get()) null
        else Path.of("$workingDirectory", "live_stack_00001.fit")
    }

    @Synchronized
    override fun stop() {
        waitForStacking.reset()

        commandLine?.writer?.println(STOP_LS_COMMAND)
        commandLine?.stop()
        commandLine = null
    }

    override fun close() {
        stop()
        workingDirectory.deleteStackingFiles()
    }

    override fun onInputRead(line: String) {
        LOG.debug { line }

        if (SUCCESSFUL_LOGS.any { line.contains(it, true) }) {
            waitForStacking.reset()
        } else if (FAILED_LOGS.any { line.contains(it, true) }) {
            failed.set(true)
            waitForStacking.reset()
        }
    }

    override fun onErrorRead(line: String) {
        LOG.debug { line }
        failed.set(true)
        waitForStacking.reset()
    }

    companion object {

        private const val REQUIRES_COMMAND = "requires 1.0.0"
        private const val CD_COMMAND = "cd"
        private const val START_LS_COMMAND = "start_ls"
        private const val LS_COMMAND = "livestack"
        private const val STOP_LS_COMMAND = "stop_ls"

        @JvmStatic private val LOG = loggerFor<SirilLiveStacker>()

        @JvmStatic private val SUCCESSFUL_LOGS = arrayOf(
            "Waiting for second image",
            "Stacked image",
        )

        @JvmStatic private val FAILED_LOGS = arrayOf(
            "Not enough stars",
            "Sequence processing partially succeeded",
        )

        @JvmStatic private val LIVE_STACK_FIT_REGEX = Regex("live_stack_\\d+.fit")
        @JvmStatic private val LIVE_STACK_SEQ_REGEX = Regex("live_stack_\\d*.seq")

        @JvmStatic
        fun Path.deleteStackingFiles() {
            for (file in listDirectoryEntries("*.fit")) {
                if (file.isSymbolicLink() && LIVE_STACK_FIT_REGEX.matches(file.name)) {
                    file.deleteIfExists()
                }
            }

            for (file in listDirectoryEntries("*.seq")) {
                if (LIVE_STACK_SEQ_REGEX.matches(file.name)) {
                    file.deleteIfExists()
                }
            }
        }
    }
}
