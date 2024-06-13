package nebulosa.siril.command

/**
 * Stops the live stacking session.
 */
data object StopLs : SirilCommand<Unit> {

    override fun write(commandLine: SirilCommandLine) {
        commandLine.write("stop_ls")
    }
}
