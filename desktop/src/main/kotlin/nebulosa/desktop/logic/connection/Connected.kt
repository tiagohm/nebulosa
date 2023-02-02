package nebulosa.desktop.logic.connection

import nebulosa.indi.client.INDIClient

data class Connected(override val client: INDIClient) : ConnectionEvent
