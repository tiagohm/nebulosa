package nebulosa.api.events

import nebulosa.indi.INDIClient

data class DisconnectedEvent(val client: INDIClient)
