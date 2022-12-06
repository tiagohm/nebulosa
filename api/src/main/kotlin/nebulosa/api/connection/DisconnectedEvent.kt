package nebulosa.api.connection

import nebulosa.indi.INDIClient

data class DisconnectedEvent(override val client: INDIClient) : ConnectionEvent
