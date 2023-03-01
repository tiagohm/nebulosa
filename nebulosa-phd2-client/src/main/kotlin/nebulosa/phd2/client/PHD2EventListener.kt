package nebulosa.phd2.client

import nebulosa.phd2.client.event.PHD2Event

fun interface PHD2EventListener {

    fun onEvent(client: PHD2Client, event: PHD2Event)
}
