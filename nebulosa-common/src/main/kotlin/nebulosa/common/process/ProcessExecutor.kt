package nebulosa.common.process

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit

open class ProcessExecutor(private val path: Path) {

    fun execute(
        arguments: Map<String, Any?>,
        timeout: Duration? = null,
        workingDir: Path? = null,
        cancellationToken: CancellationToken = CancellationToken.NONE,
    ): Process {
        val args = ArrayList<String>(arguments.size * 2)

        for ((key, value) in arguments) {
            args.add(key)
            if (value != null) args.add("$value")
        }

        args.add(0, "$path")

        val process = ProcessBuilder(args)
            .also { if (workingDir != null) it.directory(workingDir.toFile()) }
            .start()!!

        LOG.info("executing process. pid={}, args={}", process.pid(), args)

        // TODO: READ OUTPUT STREAM LINE TO CALLBACK

        cancellationToken.listen { process.destroyForcibly() }

        if (timeout == null || timeout.isNegative) process.waitFor()
        else process.waitFor(timeout.seconds, TimeUnit.SECONDS)

        return process
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ProcessExecutor>()
    }
}
