package nebulosa.siril.command

sealed interface SirilCommand<out T> {

    fun write(commandLine: SirilCommandLine): T

    companion object {

        internal const val SCRIPT_EXECUTION_FAILED = "log: Script execution failed"
    }
}
