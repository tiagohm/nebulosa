@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.*

inline fun Context.pathParamAsBoolean(key: String) = pathParamAsClass<Boolean>(key)
inline fun Context.queryParamAsBoolean(key: String) = queryParamAsClass<Boolean>(key)
inline fun Context.formParamAsBoolean(key: String) = formParamAsClass<Boolean>(key)
inline fun Context.headerAsBoolean(key: String) = headerAsClass<Boolean>(key)
