package nebulosa.common.exec

inline fun commandLine(action: CommandLine.Builder.() -> Unit): CommandLine {
    return CommandLine.Builder().also(action).get()
}
