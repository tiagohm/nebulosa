package nebulosa.api.ktor

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.utils.io.InternalAPI
import nebulosa.api.notification.Severity
import java.net.ConnectException
import java.util.concurrent.ExecutionException

data class ExceptionResponse(
    @JvmField val message: String,
    @JvmField val type: Severity = Severity.ERROR,
)

@OptIn(InternalAPI::class)
fun Application.configureRouting() {
    install(Resources)
    install(IgnoreTrailingSlash)
    install(StatusPages) {
        exception<Throwable> { call, ex ->
            val message = when (ex) {
                is ConnectException -> "connection refused"
                is NumberFormatException -> "invalid number: ${ex.message}"
                is ExecutionException -> ex.cause!!.message
                else -> ex.message
            }

            call.respond(HttpStatusCode.BadRequest, ExceptionResponse(message ?: "unknown error"))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            if (RoutingFailureStatusCode !in call.attributes) {
                call.respond(HttpStatusCode.NoContent, "")
            }
        }
    }
    // routing {
    // Static plugin. Try to access `/static/index.html`
    // staticResources("/static", "static")
    // }
}
