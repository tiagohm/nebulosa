package nebulosa.siril.command

data object Exit : SirilCommand<Unit> {

    override fun write(commandLine: SirilCommandLine) {
        commandLine.write("exit")
    }
}
