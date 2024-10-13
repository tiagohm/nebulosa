package nebulosa.api.core

import io.javalin.Javalin

interface Controller {

    val app: Javalin
}
