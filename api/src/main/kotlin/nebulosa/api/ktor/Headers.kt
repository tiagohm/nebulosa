@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.ktor

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.routing.*
import nebulosa.api.atlas.Location
import java.util.concurrent.ConcurrentHashMap

const val X_LOCATION_HEADER_KEY = "X-Location"
const val X_IDEMPOTENCY_HEADER_KEY = "X-Idempotency-Key"

private val CACHED_LOCATION = ConcurrentHashMap<String, Location>(4)

inline val RoutingCall.requestHeaders
    get() = request.headers

inline val RoutingCall.responseHeaders
    get() = response.headers

fun RoutingCall.location(mapper: ObjectMapper) = requestHeaders[X_LOCATION_HEADER_KEY]
    ?.let { value -> CACHED_LOCATION.computeIfAbsent(value) { mapper.readValue(it, Location::class.java) } }

inline fun RoutingCall.idempotencyKey() = requestHeaders[X_IDEMPOTENCY_HEADER_KEY]
