package nebulosa.phd2.client

import nebulosa.phd2.client.events.PHD2Event

fun interface PHD2EventListener {

    fun onEvent(event: PHD2Event)
}
