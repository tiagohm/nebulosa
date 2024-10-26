package nebulosa.api.http

import io.javalin.Javalin

interface Controller {

    val app: Javalin
}
