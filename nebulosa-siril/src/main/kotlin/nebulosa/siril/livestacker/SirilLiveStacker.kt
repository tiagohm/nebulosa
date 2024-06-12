package nebulosa.siril.livestacker

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLineListener
import nebulosa.livestacker.LiveStacker
import nebulosa.log.loggerFor
import nebulosa.siril.command.*
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

data class SirilLiveStacker(
    private val executablePath: Path,
    private val workingDirectory: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val use32Bits: Boolean = false,
) : LiveStacker, CommandLineListener {

    private val commandLine = SirilCommandLine(executablePath)
    private val waitForStacking = CountUpDownLatch()

    override val isRunning
        get() = commandLine.isRunning

    override val isStacking
        get() = !waitForStacking.get()

    @Synchronized
    override fun start() {
        if (!commandLine.isRunning) {
            commandLine.registerCommandLineListener(this)
            commandLine.run()

            LOG.info("live stacking started. pid={}", commandLine.pid)

            check(commandLine.execute(Cd(workingDirectory))) { "failed to run cd command" }
            check(commandLine.execute(StartLs(dark, flat, use32Bits))) { "failed to start livestacking" }
        }
    }

    @Synchronized
    override fun add(path: Path): Path? {
        return if (commandLine.isRunning && commandLine.execute(LiveStack(path))) {
            Path.of("$workingDirectory", "live_stack_00001.fit")
        } else {
            null
        }
    }

    @Synchronized
    override fun stop() {
        waitForStacking.reset()
        commandLine.execute(StopLs)
    }

    override fun close() {
        stop()
        workingDirectory.deleteStackingFiles()
    }

    override fun onLineRead(line: String) {
        // LOG.debug { line }
        LOG.info(line)
    }

    override fun onExit(exitCode: Int, exception: Throwable?) {
        LOG.info("live stacking finished. exitCode={}", exitCode, exception)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SirilLiveStacker>()

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
