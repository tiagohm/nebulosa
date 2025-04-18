package nebulosa.api.ktor

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization(mapper: ObjectMapper, streamRequestBody: Boolean = true) {
    install(ContentNegotiation) {
        val converter = JacksonConverter(mapper, streamRequestBody)
        register(ContentType.Application.Json, converter)
    }
}
