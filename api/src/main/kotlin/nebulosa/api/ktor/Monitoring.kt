package nebulosa.api.ktor

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

fun Application.configureMonitoring(enabled: Boolean = true) {
    if (enabled) {
        install(CallLogging) {
            level = Level.INFO
        }
    }
}
