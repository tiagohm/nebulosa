package nebulosa.desktop.logic.connection

import nebulosa.indi.INDIClient

data class Connected(override val client: INDIClient) : ConnectionEvent
