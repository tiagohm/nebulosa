package nebulosa.api.livestacking

import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import nebulosa.common.exec.commandLine
import nebulosa.math.Angle
import java.nio.file.Path

data class SirilLiveStacker(
    private val executablePath: Path,
    private val workingDirectory: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val rotate: Angle = 0.0,
    private val use32Bits: Boolean = false,
) : LiveStacker, LineReadListener {

    @Volatile private var process: CommandLine? = null

    @Synchronized
    override fun start() {
        if (process == null) {
            process = commandLine {
                executablePath(executablePath)
                putArg("-s", "-")
                registerLineReadListener(this@SirilLiveStacker)
            }

            process!!.whenComplete { _, e ->
                println("completed. $e")
                process = null
            }

            process!!.start()

            process!!.writer.println(REQUIRES_COMMAND)
            process!!.writer.println("$CD_COMMAND $workingDirectory")
            process!!.writer.println(buildString(256) {
                append(START_LS_COMMAND)
                if (dark != null) append(" \"-dark=$dark\"")
                if (flat != null) append(" \"-flat=$flat\"")
                if (rotate != 0.0) append(" -rotate=$rotate")
                if (use32Bits) append(" -32bits")
            })
        }
    }

    @Synchronized
    override fun add(path: Path): Path {
        process?.writer?.println("$LS_COMMAND $path")
        return path
    }

    @Synchronized
    override fun stop() {
        process?.writer?.println(STOP_LS_COMMAND)
        process = null
    }

    override fun onInputRead(line: String) {
        println(line)
    }

    override fun onErrorRead(line: String) {
        println(line)
    }

    companion object {

        private const val REQUIRES_COMMAND = "requires 1.0.0"
        private const val CD_COMMAND = "cd"
        private const val START_LS_COMMAND = "start_ls"
        private const val LS_COMMAND = "livestack"
        private const val STOP_LS_COMMAND = "stop_ls"
    }
}
