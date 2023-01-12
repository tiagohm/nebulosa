package nebulosa.desktop.connections

import nebulosa.indi.INDIClient

data class Connected(override val client: INDIClient) : ConnectionEvent
