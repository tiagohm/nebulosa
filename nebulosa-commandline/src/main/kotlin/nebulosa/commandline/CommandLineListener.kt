package nebulosa.commandline

interface CommandLineListener {

    fun onStarted() = Unit

    fun onLineRead(line: String) = Unit

    fun onExited(exitCode: Int, exception: Throwable?) = Unit
}
