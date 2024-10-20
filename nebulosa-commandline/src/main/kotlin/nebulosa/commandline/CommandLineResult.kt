package nebulosa.commandline

data class CommandLineResult(
    @JvmField val exitCode: Int,
    @JvmField val exception: Throwable?,
) {

    inline val isSuccess
        get() = exception == null

    inline val isFailure
        get() = exception != null
}
