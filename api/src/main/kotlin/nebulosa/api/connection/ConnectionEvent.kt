package nebulosa.api.connection

import nebulosa.indi.INDIClient

sealed interface ConnectionEvent {

    val client: INDIClient
}
