package nebulosa.api.connection

import nebulosa.indi.INDIClient

data class ConnectedEvent(override val client: INDIClient) : ConnectionEvent
