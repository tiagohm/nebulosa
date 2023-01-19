package nebulosa.desktop.logic.connection

import nebulosa.indi.INDIClient

sealed interface ConnectionEvent {

    val client: INDIClient
}
