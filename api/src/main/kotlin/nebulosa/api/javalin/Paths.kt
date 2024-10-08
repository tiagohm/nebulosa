@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.javalin

import io.javalin.http.Context
import io.javalin.validation.Check
import io.javalin.validation.Validation.Companion.ValidationKey
import io.javalin.validation.Validator
import java.nio.file.Path
import kotlin.io.path.exists

fun Context.pathParamAsPath(key: String) = appData(ValidationKey).validator(key, Path::class.java, pathParam(key))
fun Context.queryParamAsPath(key: String) = appData(ValidationKey).validator(key, Path::class.java, queryParam(key))
fun Context.formParamAsPath(key: String) = appData(ValidationKey).validator(key, Path::class.java, header(key))
fun Context.headerAsPath(key: String) = appData(ValidationKey).validator(key, Path::class.java, formParam(key))

inline fun Validator<Path>.exists() = check(PathExistsCheck, "does not exists")

@PublishedApi
internal data object PathExistsCheck : Check<Path?> {

    override fun invoke(p: Path?) = p == null || p.exists()
}
