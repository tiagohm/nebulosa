package nebulosa.siril.command

/**
 * Returns an error if the version of Siril is older than the one passed in argument.
 */
data object Requires : SirilCommand<Unit> {

    const val MIN_VERSION = "1.0.0"

    override fun write(commandLine: SirilCommandLine) {
        commandLine.write("requires $MIN_VERSION")
    }
}
