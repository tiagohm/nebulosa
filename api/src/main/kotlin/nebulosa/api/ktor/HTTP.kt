package nebulosa.api.ktor

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import nebulosa.api.image.ImageService.Companion.X_IMAGE_INFO_HEADER_KEY

fun Application.configureHTTP() {
    // install(Compression)
    install(CORS) {
        allowNonSimpleContentTypes = true
        anyHost()
        anyMethod()
        allowHeader(X_IDEMPOTENCY_HEADER_KEY)
        allowHeader(X_LOCATION_HEADER_KEY)
        exposeHeader(X_IMAGE_INFO_HEADER_KEY)
    }
}
