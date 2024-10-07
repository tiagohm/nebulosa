package nebulosa.api

import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import nebulosa.log.loggerFor

@Command(name = "nebulosa")
class Nebulosa : Runnable, AutoCloseable {

    @Option(name = ["-h", "--host"])
    private var host = "0.0.0.0"

    @Option(name = ["-p", "--port"])
    private var port = 0

    private lateinit var app: Javalin

    override fun run() {
        // Run the application.
        app = Javalin
            .create { it.config() }
            .start(host, port)

        LOG.info("server is started at port: {}", app.port())
    }

    override fun close() {
        app.stop()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<Nebulosa>()

        private fun JavalinConfig.config() {
            showJavalinBanner = false
        }
    }
}
