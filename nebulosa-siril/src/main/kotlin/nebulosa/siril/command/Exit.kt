package nebulosa.siril.command

/**
 * Quits the application.
 */
data object Exit : SirilCommand<Unit> {

    override fun write(commandLine: SirilCommandLine) {
        commandLine.write("exit")
    }
}
