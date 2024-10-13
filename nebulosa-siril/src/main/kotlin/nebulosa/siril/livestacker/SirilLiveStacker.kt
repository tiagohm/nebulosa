package nebulosa.siril.livestacker

import nebulosa.livestacker.LiveStacker
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.siril.command.*
import nebulosa.util.concurrency.latch.CountUpDownLatch
import nebulosa.util.exec.CommandLineListener
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

data class SirilLiveStacker(
    private val executablePath: Path,
    private val workingDirectory: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val use32Bits: Boolean = false,
) : LiveStacker, CommandLineListener {

    private val commandLine = SirilCommandLine(executablePath)
    private val waitForStacking = CountUpDownLatch()

    override val stacker = null

    override val isRunning
        get() = commandLine.isRunning

    override val isStacking
        get() = !waitForStacking.get()

    override var stackedPath: Path? = null
        private set

    @Synchronized
    override fun start() {
        if (!commandLine.isRunning) {
            commandLine.registerCommandLineListener(this)
            commandLine.run()

            LOG.d("live stacking started. pid={}", commandLine.pid)

            try {
                check(commandLine.execute(Cd(workingDirectory))) { "failed to run cd command" }
                check(commandLine.execute(StartLs(darkPath, flatPath, use32Bits))) { "failed to start livestacking" }
            } catch (e: Throwable) {
                commandLine.close()
                throw e
            }
        }
    }

    @Synchronized
    override fun add(path: Path, referencePath: Path?): Path? {
        if (commandLine.isRunning && commandLine.execute(LiveStack(path))) {
            stackedPath = Path.of("$workingDirectory", "live_stack_00001.fit")
        }

        return stackedPath
    }

    @Synchronized
    override fun stop() {
        waitForStacking.reset()
        commandLine.execute(StopLs)
        commandLine.close()
    }

    override fun close() {
        stop()
        workingDirectory.deleteStackingFiles()
    }

    override fun onExit(exitCode: Int, exception: Throwable?) {
        LOG.d("live stacking finished. exitCode={}", exitCode, exception)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SirilLiveStacker>()

        @JvmStatic private val LIVE_STACK_FIT_REGEX = Regex("live_stack_\\d+.fit")
        @JvmStatic private val LIVE_STACK_SEQ_REGEX = Regex("live_stack_\\d*.seq")

        @JvmStatic
        fun Path.deleteStackingFiles() {
            for (file in listDirectoryEntries("*.fit")) {
                if (LIVE_STACK_FIT_REGEX.matches(file.name)) {
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
