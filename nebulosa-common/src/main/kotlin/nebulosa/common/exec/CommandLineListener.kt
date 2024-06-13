package nebulosa.common.exec

interface CommandLineListener {

    fun onLineRead(line: String) = Unit

    fun onExit(exitCode: Int, exception: Throwable?) = Unit

    fun interface OnLineRead : CommandLineListener {

        override fun onLineRead(line: String)
    }

    fun interface OnExit : CommandLineListener {

        override fun onExit(exitCode: Int, exception: Throwable?)
    }
}
