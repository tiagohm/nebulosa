package nebulosa.api.ktor

import io.ktor.server.application.Application

interface Controller {

    val app: Application
}
