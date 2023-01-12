package nebulosa.desktop.connections

import nebulosa.indi.INDIClient

data class Disconnected(override val client: INDIClient) : ConnectionEvent
