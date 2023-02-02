package nebulosa.desktop.logic.connection

import nebulosa.indi.client.INDIClient

data class Disconnected(override val client: INDIClient) : ConnectionEvent
