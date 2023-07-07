package nebulosa.api

import nebulosa.log.loggerFor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.event.EventListener

@SpringBootApplication
class Nebulosa {

    @EventListener
    private fun onServletWebServerInitializedEvent(event: ServletWebServerInitializedEvent) {
        LOG.info("server is started at port: ${event.webServer.port}")
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<Nebulosa>()
    }
}
