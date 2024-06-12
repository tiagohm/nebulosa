package nebulosa.siril.command

data object Requires : SirilCommand<Unit> {

    const val MIN_VERSION = "1.0.0"

    override fun write(commandLine: SirilCommandLine) {
        commandLine.write("requires $MIN_VERSION")
    }
}
