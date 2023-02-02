package nebulosa.desktop.logic.connection

import nebulosa.indi.client.INDIClient

sealed interface ConnectionEvent {

    val client: INDIClient
}
