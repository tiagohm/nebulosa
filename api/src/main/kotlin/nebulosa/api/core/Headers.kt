@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.core

import io.javalin.http.Context
import io.javalin.json.fromJsonString
import nebulosa.api.atlas.Location
import java.util.concurrent.ConcurrentHashMap

const val X_LOCATION_HEADER_KEY = "X-Location"
const val X_IDEMPOTENCY_HEADER_KEY = "X-Idempotency-Key"

@PublishedApi internal val CACHED_LOCATION = ConcurrentHashMap<String, Location>(4)

inline fun Context.location() =
    header(X_LOCATION_HEADER_KEY)?.let { value -> CACHED_LOCATION.computeIfAbsent(value) { jsonMapper().fromJsonString<Location>(it) } }

inline fun Context.idempotencyKey() = header(X_IDEMPOTENCY_HEADER_KEY)
