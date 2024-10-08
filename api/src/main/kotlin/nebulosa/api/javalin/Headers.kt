@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import io.javalin.http.headerAsClass
import nebulosa.api.atlas.Location

const val X_LOCATION_HEADER_KEY = "X-Location"
const val X_IDEMPOTENCY_HEADER_KEY = "X-Idempotency-Key"

inline fun Context.location() = headerAsClass<Location>(X_LOCATION_HEADER_KEY).get()
inline fun Context.locationOrNull() = headerAsClass<Location>(X_LOCATION_HEADER_KEY).allowNullable().get()

inline fun Context.idempotencyKey() = headerAsClass<String>(X_IDEMPOTENCY_HEADER_KEY).notBlank().get()
inline fun Context.idempotencyKeyOrNull() = headerAsClass<String>(X_IDEMPOTENCY_HEADER_KEY).allowNullable().get()
