@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import io.javalin.http.headerAsClass
import nebulosa.api.atlas.Location

const val X_LOCATION_HEADER_KEY = "X-Location"

inline fun Context.location() = headerAsClass<Location>(X_LOCATION_HEADER_KEY).get()
inline fun Context.locationOrNull() = headerAsClass<Location>(X_LOCATION_HEADER_KEY).allowNullable().get()
