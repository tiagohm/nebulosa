@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import io.javalin.json.fromJsonString
import nebulosa.api.atlas.Location

const val X_LOCATION_HEADER_KEY = "X-Location"
const val X_IDEMPOTENCY_HEADER_KEY = "X-Idempotency-Key"

inline fun Context.location() = header(X_LOCATION_HEADER_KEY)?.let { this.jsonMapper().fromJsonString<Location>(it) }
inline fun Context.idempotencyKey() = header(X_IDEMPOTENCY_HEADER_KEY)
