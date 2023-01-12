package nebulosa.desktop.connections

import nebulosa.indi.INDIClient

sealed interface ConnectionEvent {

    val client: INDIClient
}
