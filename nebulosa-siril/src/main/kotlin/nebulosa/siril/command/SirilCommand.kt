package nebulosa.siril.command

/**
 * @see <a href="https://siril.readthedocs.io/en/stable/Commands.html">Commands</a>
 */
sealed interface SirilCommand<out T> {

    fun write(commandLine: SirilCommandLine): T

    companion object {

        internal const val SCRIPT_EXECUTION_FAILED = "log: Script execution failed"
    }
}
