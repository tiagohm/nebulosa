package nebulosa.commandline

interface CommandLineListener {

    fun onStarted(pid: Long) = Unit

    fun onLineRead(line: String) = Unit

    fun onExited(exitCode: Int, exception: Throwable?) = Unit
}
